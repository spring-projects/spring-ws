=====================================================
== Spring Web Service Airline sample application ==
=====================================================

1. INTRODUCTION

Features a web service on top of an airline reservation system, backed by a database. The web service works by using XML
Marshalling techniques (JAXB 1), and JDOM in combination with XPath queries to pull information from a message. All
messages follow following the airline.xsd schema in src/webapp.

A C# client is available in the client directory.

2. INSTALLATION

The Airline sample is a normal web application that connects to a database of your choice.

1. Create a database using one of the scripts in src/etc/db. First, initialize the database using either the MySQL or
   PostgreSQL initDB.sql script, and after that run populateDb.sql.
2. Adjust the jdbc.properties in src/main/resources/org/springframework/ws/samples/airline/dao
   to reflect your database connection settings
3. Adjust the hibernate.properties in src/main/resources/org/springframework/ws/samples/airline/dao/hibernate
4. run "mvn package" and deploy the war file generated in target; or run "mvn jetty:run" to run the sample 
   using the Jetty Web container built into Maven 2.

Note that both MySQL drivers are linked in using Maven so you don't have include these in your server if you're using
this database.

3. THE CLIENTS

The client directory contains two sample clients: one in C# and one using SAAJ. More instructions are provided in the 
readme files in the directories.

