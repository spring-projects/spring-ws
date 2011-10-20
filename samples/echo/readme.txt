=====================================================
== Spring Web Service Echo sample application ==
=====================================================


1. INTRODUCTION

This sample shows a bare-bones echoing service. Incoming messages are handled
via DOM, and a simple 'business logic' service is used to obtain the result.

2. SERVER

Simply run "mvn package" in the server directory and deploy the war file generated in 'target'.
Alternatively, run "mvn jetty:run" to run the sample in a built-in Jetty6 Web
container.

3. CLIENT

Simply run "mvn install exec:java" in each of the client subdirectories.
