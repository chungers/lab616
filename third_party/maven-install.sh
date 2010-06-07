#!/bin/bash

# Script for installing locally all required jars.  Currently the following
# packages require manual install (not available in remote Maven repositories):
#
# 1. Tomcat native library  (Note Tomcat APR is not managed by Maven)
# 2. Interactivebrokers TWS
#

function install {
    jar_path=$1
    group_id=$2
    artifact_id=$3
    version=$4
    shift;shift;shift;shift;
#cat <<EOF   
    mvn $@ install:install-file \
	-Dfile=${jar_path} \
	-DgroupId=${group_id} \
	-DartifactId=${artifact_id} \
	-Dversion=${version} \
	-Dpackaging=jar \
	-DgeneratePom=true
#EOF
}

####################################################################################################
# Tomcat native

TOMCAT_NATIVE_DIR=java/tomcat-native-1.1.17-src/jni/dist
TOMCAT_NATIVE_VERSION=1.1.17
install ${TOMCAT_NATIVE_DIR}/tomcat-native.jar tomcat-native tomcat-native ${TOMCAT_NATIVE_VERSION} $@


####################################################################################################
# Interactive Brokers TWS Client API

TWS_API_DIR=java/ib-twsapi-964/IBJts
TWS_API_VERSION=964
install ${TWS_API_DIR}/jtsclient.jar com.interactivebrokers com.interactivebrokers.api ${TWS_API_VERSION} $@


####################################################################################################
# Interactive Brokers TWS + Gateway Client

TWS_DIR=java/ib-tws-905/IBJts
TWS_VERSION=905
TWS_JARS=`ls ${TWS_DIR}/*.jar`
TWS_GROUP="com.interactivebrokers"
TWS_PREFIX="com.interactivebrokers.tws."
for i in ${TWS_JARS}; do
    install $i ${TWS_GROUP} "${TWS_PREFIX}`basename ${i}`" ${TWS_VERSION} $@
done

# Top level application dependency:
TWS_POM=/tmp/ibtws.pom
cat <<EOF > ${TWS_POM}
<?xml version="1.0" encoding="UTF-8"?><project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.interactivebrokers</groupId>
  <artifactId>com.interactivebrokers.tws.jts</artifactId>
  <version>${TWS_VERSION}</version>
  <packaging>jar</packaging>
  <description>POM created to include all dependencies</description>
  <dependencies>
EOF
for i in ${TWS_JARS}; do
    cat <<EOF >> ${TWS_POM}
    <dependency>
      <groupId>${TWS_GROUP}</groupId>
      <artifactId>${TWS_PREFIX}`basename $i`</artifactId>
      <version>${TWS_VERSION}</version>
    </dependency>
EOF
done
cat <<EOF >> ${TWS_POM}
  </dependencies>
</project>
EOF
mvn install:install-file -Dfile=${TWS_DIR}/jts.jar -DpomFile=${TWS_POM}
