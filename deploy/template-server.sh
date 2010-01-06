#!/bin/bash

# Global defaults:
BIN_NAME=`basename $0 | awk -F. '{print $1}'`
PROFILE=$1;
shift;
PACKAGE_DIR=`dirname $0`
BINARY_DIR=${PACKAGE_DIR}/bin
WORKING_DIR=${PACKAGE_DIR}/local
LOGFILE=${PACKAGE_DIR}/${BIN_NAME}.${PROFILE}.log
PROFILE_PROPERTIES=${PACKAGE_DIR}/profiles/${PROFILE}
PID=${PACKAGE_DIR}/${BIN_NAME}.${PROFILE}.pid
TZ="US/Eastern"

echo "DIR = $PACKAGE_DIR"
case `uname` in
Linux)
	export JAVA_HOME=/opt/jdk1.6
	# export JAVA_HOME=/home/lab616/jrrt-3.1.0-1.6.0
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

echo `which java`
echo `java -version`

JAR=${BINARY_DIR}/${BIN_NAME}.jar
JVM_ARGS="-Djava.library.path=${BINARY_DIR} -server -Xmx1024M -Duser.timezone=${TZ}"

# MAIN_CLASS = [ JAR | <class_name> ].  This is substituted by borg script for --newserver <class_name>
MAIN_CLASS=@MAIN_CLASS

if [[ "$MAIN_CLASS" == "JAR" ]]; then
    JAVA_COMMAND="nohup java ${JVM_ARGS} -jar ${JAR} --profile=${PROFILE_PROPERTIES}"
else
    JAVA_COMMAND="nohup java ${JVM_ARGS} -classpath ${JAR} ${MAIN_CLASS} --profile=${PROFILE_PROPERTIES}"
fi

###########################################################
# Package-specific parameters:

ARGS=""
${JAVA_COMMAND} ${ARGS} $@ &>${LOGFILE} &

# Send the pid to a file (last process captured in $!)
echo $! > ${PID}

#tail -f ${LOGFILE}
