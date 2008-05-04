@echo off
set WCF_PATH=%WINDIR%\Microsoft.NET\Framework\v3.0\Windows Communication Foundation
svcutil /noLogo /out:src\StockService.cs -namespace:http://www.springframework.org/spring-ws/samples/stockquote,Spring.Ws.Samples.Stock.Client.Wcf /noConfig http://localhost:8080/StockService.wsdl
csc /noLogo /out:bin\client.exe src\Main.cs src\StockService.cs 
copy src\*.config bin\