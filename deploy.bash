#!/bin/bash -x

if [ "${CIRCLE_BRANCH}" == "master" ] || [ "${CIRCLE_BRANCH}" == "2.x" ]; then
	./mvnw -Pdistribute,snapshot,docs clean deploy
else
	echo "We only deploy 'master' and '2.x' branches"
fi