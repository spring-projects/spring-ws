#!/bin/sh
mvn -Psamples -Ddescriptor=src/assembly/full.xml clean install javadoc:javadoc assembly:assembly
