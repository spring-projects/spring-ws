#!/bin/bash -x

set -euo pipefail

STAGING_REPOSITORY_ID=$1

echo 'Release to Maven central...'

MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ./mvnw \
    -s settings.xml \
    -Pcentral  \
    -DstagingRepositoryId="${STAGING_REPOSITORY_ID}" \
    nexus-staging:rc-release

