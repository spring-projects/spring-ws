set WSE_PATH="C:\Program Files\Microsoft WSE\v2.0"
%WSE_PATH%\Tools\Wsdl\WseWsdl2 ..\..\src\main\webapp\airline.wsdl src\AirlineService-wse.cs 
csc -out:bin\airline-wse.exe -reference:%WSE_PATH%\Microsoft.Web.Services2.dll src\wse.cs src\AirlineService-wse.cs