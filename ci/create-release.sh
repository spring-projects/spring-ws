#!/bin/bash

set -euo pipefail

RELEASE=$1
SNAPSHOT=$2

# Bump up the version in pom.xml to the desired version and commit the change
./mvnw versions:set -DnewVersion=$RELEASE -DgenerateBackupPoms=false
git add .
git commit --message "Releasing Spring Web Services v$RELEASE"

# Tag the release
git tag -s v$RELEASE -m "v$RELEASE"

# Bump up the version in pom.xml to the next snapshot
./mvnw versions:set -DnewVersion=$SNAPSHOT -DgenerateBackupPoms=false
git add .
git commit --message "Continue development on v$SNAPSHOT"


