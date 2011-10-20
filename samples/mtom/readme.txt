====================================
== Spring Web Service MTOM Sample ==
====================================


1. INTRODUCTION

This sample shows how to use marshal and unmarshal MTOM attachments using JAXB2.

2. SERVER

Simply run "mvn package" in the server directory and deploy the war file generated in 'target'.
Alternatively, run "mvn jetty:run" to run the sample in a built-in Jetty6 Web
container.

3. CLIENT

Simply run "mvn install exec:java" in each of the client subdirectories.
