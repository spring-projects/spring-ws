#!/bin/bash

set -euo pipefail

MAVEN_OPTS="-Duser.name=spring-builds+jenkins -Duser.home=/tmp/jenkins-home" \
  ./mvnw -s settings.xml \
  -P${PROFILE} clean dependency:list test -Dsort -B -U
# Provides for testing org.springframework.ws.observation.ObservationInWsConfigurerTests separately
MAVEN_OPTS="-Duser.name=spring-builds+jenkins -Duser.home=/tmp/jenkins-home" \
  ./mvnw -s settings.xml \
  -P-default,observation clean dependency:list test -Dsort -B -U
