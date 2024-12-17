#!/bin/bash

set -euo pipefail

export GRADLE_ENTERPRISE_CACHE_USERNAME=${GRADLE_ENTERPRISE_CACHE_USR}
export GRADLE_ENTERPRISE_CACHE_PASSWORD=${GRADLE_ENTERPRISE_CACHE_PSW}

MAVEN_OPTS="-Duser.name=spring-builds+jenkins -Duser.home=/tmp/jenkins-home" \
  ./mvnw -s settings.xml \
  -P${PROFILE} clean dependency:list test -Dsort -B -U
# Provides for testing org.springframework.ws.observation.ObservationInWsConfigurerTests separately
MAVEN_OPTS="-Duser.name=spring-builds+jenkins -Duser.home=/tmp/jenkins-home" \
  ./mvnw -s settings.xml \
  -P-default,observation clean dependency:list test -Dsort -B -U
