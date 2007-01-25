#!/bin/sh
mvn -Pdoc -Ddescriptor=src/assembly/full.xml clean install javadoc:javadoc assembly:assembly
