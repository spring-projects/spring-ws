#!/bin/bash -x

if [ "${CIRCLE_BRANCH}" == "master" ] || [ "${CIRCLE_BRANCH}" == "2.x" ]; then
	./mvnw -Pdistribute,snapshot,docs clean deploy
fi