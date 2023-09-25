#!/bin/bash -x

set -euo pipefail

STAGING_REPOSITORY_ID=$1

echo 'Release to Maven central...'

MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home --add-opens=java.base/java.util=ALL-UNNAMED " ./mvnw \
    -s settings.xml \
    -Pcentral  \
    -DstagingRepositoryId="${STAGING_REPOSITORY_ID}" \
    nexus-staging:rc-release

