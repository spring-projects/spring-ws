SPRING WEB SERVICES

This directory contains two C# clients for the Airline Web Service. One sample (airline.exe) uses non-secure 
functionality, and works under both .NET and Mono, while getmileage.exe only works under .NET because it relies on 
Microsoft's Web Services enhancements (WSE 2.0) to perform WS-Security.

The C# client is executable: just run the executable file and (if the war file is deployed at the default 
http://localhost:8080/airline) the web service will be called, causing a flight reservations to be created.

If the web service is NOT running at the default URL, you can append the URL argument to the exectuable file
(e.g. 'airline.exe http://localhost:8080/airline-webservice/Airline').


C# Client Sample table of contents
---------------------------------------------------
* bin - The executable files for the clients: airline.exe and getmileage.exe
* src - The source files for the clients.
* Makefile - Mono makefile.
* build.bat -  .NET build batch file for airline.exe
* build-getmileage.bat - .NET build batch file for getmileage.exe