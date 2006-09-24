=====================================================
== Spring Web Service JPetStore sample application ==
=====================================================

@author Alef Arendsen

1. INTRODUCTION

Features a web service on top of an airline reservation system, backed by a database.
The web service works using XPath queries to pull information from a message following
the airline.xsd schema in src\webapp.

A c# client is available in the client directory.

2. INSTALLATION

The Airline sample is a normal web application that connects to a database of your
choice.

1. Create a database using one of the scripts in src/etc/db
2. Adjust the jdbc.properties in src/etc/resources/org/springframework/ws/samples/airline/dao
   to reflect your database connection settings
3. Adjust the hibernate.propeties in src/etc/resources/org/springframework/ws/samples/airline/dao/hibernate
4. run 'ant war' and deploy the war file generated in target/artifacts/war

Note that both MySQL and PostgreSQL drivers are linked in using Ivy so you don't have
include these in your server if you're using either one of those databases.

3. RUNNING THE CLIENT

The c# client is available in the client directory. Just run the airline.exe file and
(if you've deployed te war file without changing the default--causing the URL of the 
airline service to be http://localhost:8080/airline/Airline) the web service will be
called, causing two flight reservations to be created.

If the web service is NOT running at the default URL, you can append the URL to the
airline.exe file (e.g. 'airline.exe http://localhost:8080/airline-webservice/Airline').


