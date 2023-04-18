#!/bin/bash -x

set -euo pipefail

RELEASE_TYPE=$1

echo 'Deploying to Artifactory...'

MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ./mvnw \
  -s settings.xml \
  -Pdistribute,${RELEASE_TYPE} \
  -Dmaven.test.skip=true \
  clean deploy -B
