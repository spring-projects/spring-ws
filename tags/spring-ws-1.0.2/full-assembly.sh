#!/bin/sh
mvn -Ddescriptor=src/assembly/full.xml -Dmaven.test.skip=true clean install javadoc:javadoc docbkx:generate-html docbkx:generate-pdf assembly:assembly
