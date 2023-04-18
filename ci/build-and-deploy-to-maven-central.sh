#!/bin/bash -x

set -euo pipefail

PROJECT_VERSION=$1
STAGING_REPOSITORY_ID=$2

echo 'Staging on Maven Central...'

GNUPGHOME=/tmp/gpghome
export GNUPGHOME

mkdir $GNUPGHOME
cp $KEYRING $GNUPGHOME

MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ./mvnw \
    -s settings.xml \
    -Pdistribute,central  \
    -Dmaven.test.skip=true \
    -Dgpg.passphrase=${PASSPHRASE} \
    -Dgpg.secretKeyring=${GNUPGHOME}/secring.gpg \
    -DstagingDescription="Releasing ${PROJECT_VERSION}" \
    -DstagingRepositoryId="${STAGING_REPOSITORY_ID}" \
    clean deploy -B

