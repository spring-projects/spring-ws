#!/bin/bash -x

set -euo pipefail

RELEASE_TYPE=$1

export GRADLE_ENTERPRISE_CACHE_USERNAME=${GRADLE_ENTERPRISE_CACHE_USR}
export GRADLE_ENTERPRISE_CACHE_PASSWORD=${GRADLE_ENTERPRISE_CACHE_PSW}

echo 'Deploying to Artifactory...'

MAVEN_OPTS="-Duser.name=spring-builds+jenkins -Duser.home=/tmp/jenkins-home" ./mvnw \
  -s settings.xml \
  -Pdistribute,${RELEASE_TYPE} \
  -Dmaven.test.skip=true \
  clean deploy -B
