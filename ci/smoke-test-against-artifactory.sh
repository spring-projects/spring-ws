#!/bin/bash -x

set -euo pipefail

PROJECT_VERSION=$1

echo 'Smoke test against Artifactory...'

cd smoke-tests

MAVEN_OPTS="-Duser.name=spring-builds+jenkins -Duser.home=/tmp/jenkins-home" ./mvnw \
    -Partifactory  \
    -Dspring-ws.version="${PROJECT_VERSION}" \
    clean dependency:purge-local-repository verify -B -U

