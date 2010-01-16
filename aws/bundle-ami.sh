#!/bin/bash

REMOTE_HOST=${1}
IMAGE_NAME=${2}
REMOTE_USER=${3}

DOMAIN=lab616.com

ABSPATH=$(cd ${0%/*} && echo $PWD/${0##*/})
AWS_DIR=`dirname "$ABSPATH"`

echo "AWS_DIR=$AWS_DIR"

case `uname` in
Linux)
	export JAVA_HOME=/opt/jdk1.6
;;
Darwin)
	export JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.5/Home
;;
*)
	echo "JAVA_HOME must be set."
	exit -1
;;
esac

export EC2_HOME=${AWS_DIR}/ec2-tools
export PATH=$JAVA_HOME:$EC2_HOME/bin:$PATH

echo "EC2_HOME = $EC2_HOME"

ACCOUNT_CREDENTIALS="${AWS_DIR}/.account/${DOMAIN}"
KEY_DIR=${ACCOUNT_CREDENTIALS}/keys

export EC2_PRIVATE_KEY=${ACCOUNT_CREDENTIALS}/pk-EQQBM3IKMMJCI4RMXN7CH4ZRGHQSLSXI.pem 
export EC2_CERT=${ACCOUNT_CREDENTIALS}/cert-EQQBM3IKMMJCI4RMXN7CH4ZRGHQSLSXI.pem 

AWS_USER_ID=`cat ${ACCOUNT_CREDENTIALS}/account-id`
AWS_ACCESS_KEY_ID=`cat ${ACCOUNT_CREDENTIALS}/access-key-id`
AWS_SECRET_ACCESS_KEY=`cat ${ACCOUNT_CREDENTIALS}/secret-access-key`

PACKAGE_SCRIPT="/tmp/package-ami.sh"
BUCKET="com.lab616.ami"
PREFIX="${IMAGE_NAME}"


# Copy key and cert to instance ephemeral storage
scp -i ${KEY_DIR}/dev.pem ${ACCOUNT_CREDENTIALS}/{cert,pk}-*.pem ${REMOTE_USER}@${REMOTE_HOST}:/tmp

# Determine the architecture of the remote instance.
ARCH=`ssh -i ${KEY_DIR}/dev.pem ${REMOTE_USER}@${REMOTE_HOST} arch`
#ARCH="i386"

# Build a script to run on the remote instance:
cat | ssh -i ${KEY_DIR}/dev.pem ${REMOTE_USER}@${REMOTE_HOST} <<EOF1
cat > ${PACKAGE_SCRIPT} <<EOF
#!/bin/bash

# Bundle AMI
sudo rm -f /mnt/${PREFIX}*
sudo -E ec2-bundle-vol -r ${ARCH} -d /mnt -p ${PREFIX} -u ${AWS_USER_ID} -k /tmp/pk-*.pem -c /tmp/cert-*.pem -s 10240 -e /mnt,/tmp,/root/.ssh,/data,/git

# Upload AMI to S3
ec2-upload-bundle -b ${BUCKET} -m /mnt/${PREFIX}.manifest.xml -a ${AWS_ACCESS_KEY_ID} -s ${AWS_SECRET_ACCESS_KEY}

# Remove the script file
#rm -f ${PACKAGE_SCRIPT}

EOF
EOF1

# Change the permission of the bundle script
ssh -i ${KEY_DIR}/dev.pem ${REMOTE_USER}@${REMOTE_HOST} chmod a+x ${PACKAGE_SCRIPT}

# Run the bundle script
ssh -i ${KEY_DIR}/dev.pem ${REMOTE_USER}@${REMOTE_HOST} ${PACKAGE_SCRIPT}

# Register the ami, from local client:
ec2-register ${BUCKET}/${PREFIX}.manifest.xml
