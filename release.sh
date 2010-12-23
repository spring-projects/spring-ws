#!/bin/sh
MAVEN_OPTS="-Djava.awt.headless=true -Xmx512m -Xms64m -XX:MaxPermSize=128m"
mvn -Dmaven.test.skip=true -P release clean javadoc:javadoc docbkx:generate-html docbkx:generate-pdf install
