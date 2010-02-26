SPRING WEB SERVICES

Sample table of contents
---------------------------------------------------
* airline - A complete airline sample that shows both Web Service and 
  O/X Mapping functionality in a complete application
* echo - A simple sample that shows a bare-bones Echo service
* mtom - Shows how to use MTOM and JAXB2 marshalling
* pox - Shows Plain Old XML usage
* stock - Shows how to use WS-Addressing and the Java 6 HTTP Server
* tutorial - Contains the code from the Spring-WS tutorial

Except the tutorial, all of these samples consist of a separate 'server' and 'client' project.
The server projects can be run using the "mvn jetty:run" command, or by using "mvn package" and deploying the resulting
war archives to a Web Container.
The client projects are typically command-line projects, and can by started by issuing the 'mvn install exec:java' command.