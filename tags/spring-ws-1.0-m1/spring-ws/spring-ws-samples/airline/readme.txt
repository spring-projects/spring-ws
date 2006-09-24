=====================================================
== Spring Web Service Airline sample application ==
=====================================================

1. INTRODUCTION

Features a web service on top of an airline reservation system, backed by a database. The web service works by using XML
Marshalling techniques, and JDOM in combination with XPath queries to pull information from a message following the
airline.xsd schema in src/webapp.

A C# client is available in the client directory.

2. INSTALLATION

The Airline sample is a normal web application that connects to a database of your choice.

1. Create a database using one of the scripts in src/etc/db
2. Adjust the jdbc.properties in src/etc/resources/org/springframework/ws/samples/airline/dao
   to reflect your database connection settings
3. Adjust the hibernate.propeties in src/etc/resources/org/springframework/ws/samples/airline/dao/hibernate
4. run 'ant war' and deploy the war file generated in target/artifacts/war

Note that both MySQL and PostgreSQL drivers are linked in using Ivy so you don't have
include these in your server if you're using either one of those databases.

3. RUNNING THE CLIENTS

The client directory contains two sample clients: one in C# and one using SAAJ. Both clients are executable: just run
the executable file and (if the war file is deployed at the default http://localhost:8080/airline) the
web service will be called, causing a flight reservations to be created.

If the web service is NOT running at the default URL, you can append the URL argument to the exectuable file
(e.g. 'airline.exe http://localhost:8080/airline-webservice/Airline').


