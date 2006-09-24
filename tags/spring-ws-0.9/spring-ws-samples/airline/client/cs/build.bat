rem wsdl -out:src\AirlineService.cs ..\..\src\webapp\airline.wsdl ..\..\src\webapp\airline.xsd
csc -out:bin\airline.exe src\main.cs src\AirlineService.cs