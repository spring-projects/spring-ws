=========================================================
== Spring Web Service Plain Old XML sample application ==
=========================================================


1. INTRODUCTION

This sample shows a service that uses Plain Old XML rather than SOAP. Incoming
messages are handled via SAX, and a response is created using DOM.

2. SERVER

Simply run "mvn package" in the server directory and deploy the war file generated in 'target'.
Alternatively, run "mvn jetty:run" to run the sample in a built-in Jetty6 Web
container.

3. CLIENT

Simply run "mvn install exec:java" in each of the client subdirectories.
