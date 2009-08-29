#!/bin/bash

# Global defaults:
PROFILE=$1
PACKAGE_DIR=`dirname $0`
BINARY_DIR=${PACKAGE_DIR}/bin
WORKING_DIR=${PACKAGE_DIR}/local
LOGFILE=$0.${PROFILE}.log
PID=$0.${PROFILE}.pid
TZ="US/Eastern"

echo "DIR = $PACKAGE_DIR"
case `uname` in
Linux)
	export JAVA_HOME=/opt/jdk1.6
	# Start vncserver
	vncserver :1
	export DISPLAY=localhost:1
;;
Darwin)
	export JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home
;;
*)
	echo "JAVA_HOME must be set."
	exit -1
;;
esac

export PATH=${JAVA_HOME}/bin:$PATH

# Script-specific parameters:
profile=`cat ${PACKAGE_DIR}/profiles/${PROFILE}`
string_prompt=`echo ${profile} | awk -F":" '{ print $1 }'`
password_prompt=`echo ${profile} | awk -F":" '{ print $2 }'`
folder_prompt=${WORKING_DIR}

JAR=${BINARY_DIR}/tws.jar
JVM_ARGS="-server -Xmx1024M -Duser.timezone=${TZ}"

echo `which java`
echo `java -version`

# Copied from Eclipe run configuration.
nohup java ${JVM_ARGS} -jar ${JAR} --login=${string_prompt} --password=${password_prompt} --tws_dir=${folder_prompt} &>${LOGFILE} &

# Send the pid to a file (last process captured in $!)
echo $! > ${PID}
