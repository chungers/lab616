#!/bin/bash

# Global defaults:
BIN_NAME=`basename $0 | awk -F. '{print $1}'`
PACKAGE_DIR=`dirname $0`
BINARY_DIR=${PACKAGE_DIR}/bin
WORKING_DIR=${PACKAGE_DIR}/local
TZ="US/Eastern"

echo "DIR = $PACKAGE_DIR"
case `uname` in
Linux)
	# export JAVA_HOME=/usr/lib/jvm/java-6-sun
	export JAVA_HOME=/home/lab616/jrrt-3.1.0-1.6.0
	# Start vncserver
	vncserver :0
	export DISPLAY=localhost:0.0
;;
Darwin)
	export JAVA_HOME=/System/Library/Frameworkos/JavaVM.framework/Versions/1.6/Home
;;
*)
	echo "JAVA_HOME must be set."
	exit -1
;;
esac

export PATH=${JAVA_HOME}/bin:$PATH

echo `which java`
echo `java -version`

JAR=${BINARY_DIR}/`basename $0 | awk -F. '{print $1}'`.jar
PLATFORM_BIN="`uname`-`arch`"
JVM_ARGS="-Djava.library.path=${BINARY_DIR}/${PLATFORM_BIN} -Duser.timezone=${TZ}"

JAVA_COMMAND="java ${JVM_ARGS} -jar ${JAR}"

###########################################################
# Package-specific parameters:

ARGS=""
${JAVA_COMMAND} ${ARGS} $@
