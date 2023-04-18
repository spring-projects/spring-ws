#!/bin/bash -x

set -euo pipefail

echo "Opening a remote repository for this release..."

MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ./mvnw \
    -s settings.xml \
    -Pcentral \
    -DstagingProfileId=${STAGING_PROFILE_ID} \
    -DopenedRepositoryMessageFormat="<repository>%s</repository>" \
    nexus-staging:rc-open

