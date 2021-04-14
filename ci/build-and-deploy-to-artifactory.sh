#!/bin/bash -x

set -euo pipefail

#
# Deploy the artifactory
#
echo 'Deploying to Artifactory...'

MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" \
  ./mvnw -s settings.xml -P${PROFILE} -Dmaven.test.skip=true clean deploy -B
