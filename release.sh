#!/bin/sh
mvn -Dmaven.test.skip=true clean javadoc:javadoc docbkx:generate-html docbkx:generate-pdf install
