#!/bin/bash -x

set -euo pipefail

STAGING_PROFILE_ID=$1

echo 'Closing remote repository on Maven Central...'

MAVEN_OPTS="-Duser.name=spring-builds+jenkins -Duser.home=/tmp/jenkins-home" ./mvnw \
    -s settings.xml \
    -Pcentral \
    -DstagingRepositoryId=${STAGING_PROFILE_ID} \
    nexus-staging:rc-close

