#!/bin/bash -x

set -euo pipefail

PROJECT_VERSION=$1
STAGING_REPOSITORY_ID=$2

echo 'Smoke test against Maven Central...'

cd smoke-tests

MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ./mvnw \
    -Pmaven-central  \
    -DstagingRepositoryId="${STAGING_REPOSITORY_ID}" \
    -Dspring-ws.version="${PROJECT_VERSION}" \
    clean dependency:purge-local-repository verify -B -U

echo "Smoke tests passed. Don't smoke!"