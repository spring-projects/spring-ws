@echo off
set WCF_PATH="%WINDIR%\Microsoft.NET\Framework\v3.0\Windows Communication Foundation"
svcutil /noLogo /out:src\ImageRepositoryService.cs -namespace:http://www.springframework.org/spring-ws/samples/mtom,Spring.Ws.Samples.Mtom.Client.Wcf /noConfig http://localhost:8080/mtom/mtom.wsdl
csc /noLogo /out:bin\image.exe /reference:%WCF_PATH%\System.ServiceModel.dll src\Main.cs src\ImageRepositoryService.cs 
copy src\*.config bin\