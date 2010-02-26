=====================================================
== Spring Web Service Airline sample application ==
=====================================================

1. INTRODUCTION

Features a web service on top of an airline reservation system, backed by a database. The web service works by using XML
Marshalling techniques (JAXB2), and XPath in combination with XPath queries to pull information from a message.
Additionally, the Airline service has JMS support. All messages follow the messages.xsd schema in src/main/webapp.

2. INSTALLATION

The Airline sample is a normal web application that connects to an embedded
HSQLDB database. It requires JDK 1.5 to run; JDK 1.6 works as well, except for
the WS-Security endpoints.

To execute the sample, run "mvn jetty:run" in the server directory to run the sample in the Jetty6 Web container.

To create a war file instead of running in Jetty, use "mvn package".

3. THE CLIENTS

Simply run "mvn install exec:java" in each of the client subdirectories.
