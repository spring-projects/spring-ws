#!/bin/bash -x

set -euo pipefail

[[ -d $PWD/maven && ! -d $HOME/.m2 ]] && ln -s $PWD/maven $HOME/.m2

spring_ws_artifactory=$(pwd)/spring-ws-artifactory

rm -rf $HOME/.m2/repository/org/springframework/ws 2> /dev/null || :

cd spring-ws-github

./mvnw -Pdistribute,snapshot,docs -Dmaven.test.skip=true clean deploy \
    -DaltDeploymentRepository=distribution::default::file://${spring_ws_artifactory}
