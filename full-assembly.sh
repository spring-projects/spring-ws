#!/bin/sh
mvn clean install assembly:assembly -D descriptor=src/assembly/full.xml
