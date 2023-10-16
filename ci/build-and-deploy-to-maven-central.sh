#!/bin/bash -x

set -euo pipefail

PROJECT_VERSION=$1
STAGING_REPOSITORY_ID=$2

export GRADLE_ENTERPRISE_CACHE_USERNAME=${GRADLE_ENTERPRISE_CACHE_USR}
export GRADLE_ENTERPRISE_CACHE_PASSWORD=${GRADLE_ENTERPRISE_CACHE_PSW}

echo 'Staging on Maven Central...'

GNUPGHOME=/tmp/gpghome
export GNUPGHOME

mkdir $GNUPGHOME
cp $KEYRING $GNUPGHOME

MAVEN_OPTS="-Duser.name=spring-builds+jenkins -Duser.home=/tmp/jenkins-home" ./mvnw \
    -s settings.xml \
    -Pjakarta-ee-10,distribute,central  \
    -Dmaven.test.skip=true \
    -Dgpg.passphrase=${PASSPHRASE} \
    -Dgpg.secretKeyring=${GNUPGHOME}/secring.gpg \
    -DstagingDescription="Releasing ${PROJECT_VERSION}" \
    -DstagingRepositoryId="${STAGING_REPOSITORY_ID}" \
    clean deploy -B

