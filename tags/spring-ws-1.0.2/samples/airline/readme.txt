=====================================================
== Spring Web Service Airline sample application ==
=====================================================

1. INTRODUCTION

Features a web service on top of an airline reservation system, backed by a database. The web service works by using XML
Marshalling techniques (JAXB2), and XPath in combination with XPath queries to pull information from a message. All
messages follow the airline.xsd schema in src/main/webapp.

Multiple clients are available, showing interoperability with Axis 1, SAAJ, C#, and more.

2. INSTALLATION

The Airline sample is a normal web application that connects to a database of your choice. By using Maven2 profiles, it
supports three databases: HSQLDB (the easiest to setup), MySQL, or PostgreSQL.

To execute the sample with the supplied HSQLDB:

1. Start a command shell in the subdirectory hsqldb, and run "ant". This starts a HSQLDB server with a database named
   airline.
2. Run "mvn -P hsqldb sql:execute" to create the schema and insert data into the database.
3. Run "mvn -P hsqldb jetty:run" to run the sample in the Jetty6 Web container.

To execute the sample with MySQL or PostgreSQL:

1. Change the properties in the pom.xml file to reflect your database connection settings. There are three profiles
   (one for each supported database), so make sure to edit the right section. The areas to change are indicated with
   comments.
2. Run "mvn -P <database> sql:execute", where <database> is "mysql" or "postgresql", to create the schema and insert
   data into the database.
3. Run "mvn -P <database> jetty:run", where <database> is "mysql" or "postgresql", to run the sample in the Jetty6
   Web container.

To create a war file instead of running in Jetty, follow the steps above, but replace the "jetty:run" with "package" in
the last step.

3. THE CLIENTS

The client directory contains a number of sample clients. More instructions are provided in the readme files in the
directories.