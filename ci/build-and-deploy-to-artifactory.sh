#!/bin/bash -x

set -euo pipefail

RELEASE_TYPE=$1

echo 'Deploying to Artifactory...'

MAVEN_OPTS="-Duser.name=spring-builds+jenkins -Duser.home=/tmp/jenkins-home" ./mvnw \
  -s settings.xml \
  -Pjakarta-ee-10,distribute,${RELEASE_TYPE},default \
  -Dmaven.test.skip=true \
  clean deploy -B
