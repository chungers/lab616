#!/bin/bash
mvn install:install-file -Dfile=jcl-core-2.2.jar -DgroupId=org.xeustechnologies \
       -DartifactId=jcl-core -Dversion=2.2 -Dpackaging=jar -DgeneratePom=true
