#!/bin/sh
mvn -Dmaven.test.skip=true -P release clean javadoc:javadoc docbkx:generate-html docbkx:generate-pdf install
