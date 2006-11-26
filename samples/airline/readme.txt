=====================================================
== Spring Web Service Airline sample application ==
=====================================================

1. INTRODUCTION

Features a web service on top of an airline reservation system, backed by a database. The web service works by using XML
Marshalling techniques (JAXB 1), and JDOM in combination with XPath queries to pull information from a message. All
messages follow the airline.xsd schema in src/main/webapp.

Multiple clients are available, showing interoperability with Axis 1, SAAJ, C#, JMS and more.

2. INSTALLATION

The Airline sample is a normal web application that connects to a database of your choice.

1. Create a database using one of the scripts in src/etc/db. First, initialize the database using either the MySQL or
   PostgreSQL initDB.sql script, and after that run src/etc/db/populateDb.sql.
2. Adjust the jdbc.properties in src/main/resources/org/springframework/ws/samples/airline/dao
   to reflect your database connection settings. By default MySQL is used.
3. Adjust the hibernate.properties in src/main/resources/org/springframework/ws/samples/airline/dao/hibernate. By
   default MySQL is used.
4. (Optional) Adjust the applicationContext-ws-jms.xml in src/main/resources/org/springframework/ws/samples/airline/ws/
   to reflect your JMS connections settings. By default ActiveMQ 2.1 is used. Make sure to start ActiveMQ before  
4. run "mvn package" and deploy the war file generated in target; or run "mvn jetty:run" to run the sample 
   using the Jetty Web container built into Maven 2.
5.

Note that both MySQL drivers are linked in using Maven so you don't have include these in your server if you're using
this database.

3. THE CLIENTS

The client directory contains a number of sample clients. More instructions are provided in the readme files in the
directories.

