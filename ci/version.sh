#!/bin/bash

set -euo pipefail

VERSION=`MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ./mvnw \
  org.apache.maven.plugins:maven-help-plugin:3.4.0:evaluate \
  -Dexpression=project.version -q -DforceStdout | tail -1`

echo $VERSION

