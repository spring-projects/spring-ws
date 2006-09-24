SPRING WEB SERVICES 1.0-M2 (September 2006)
-------------------------------
http://www.springframework.org/spring-ws
http://forum.springframework.org/forumdisplay.php?f=39

1. INTRODUCTION

Spring Web Services (Spring-WS) is a product of the Spring community focused on creating document-driven Web services.
Spring-WS aims to facilitate contract-first SOAP service development, allowing for the creation of flexible web services
using one of the many ways to manipulate XML payloads.

Spring-WS consists of two major modules: a flexible Object/XML Mapping abstraction with support for JAXB 1 and 2,
XMLBeans, Castor, JiBX and XStream; and a Web service framework that resembles Spring MVC.

2. RELEASE INFO

Spring-WS requires J2SE 1.4 and J2EE 1.4.  J2SE 1.5 is required for building.

Release contents:

"." contains Spring-WS distribution units (jars and source zip archives), readme, and copyright
"docs" contains the Spring-WS reference manual and API Javadocs
"lib" contains dependencies
"modules" contains buildable modules
"modules/spring-oxm" contains buildable Spring O/X Mapping sources
"modules/spring-ws-core" contains buildable Spring-WS core sources
"modules/spring-ws-security" contains buildable Spring-WS security sources
"modules/spring-xml" contains buildable Spring XML utility sources, an internal library used by Spring-WS
"samples" contains buildable Spring-WS sample application sources

See the readme.txt within the above directories for additional information.

Spring-WS is released under the terms of the Apache Software License (see license.txt).

3. DISTRIBUTION JAR FILES

The following distinct jar files are included in the distribution. This list specifies the respective contents and
third-party dependencies. Libraries in [brackets] are optional, i.e. just necessary for certain functionality.

* spring-oxm-1.0-m2.jar
- Contents: The Spring Object/XML Mapping framework
- Dependencies: Commons Logging, spring-beans, spring-core
                [Log4J, JAXB, Castor, XMLBeans, StAX, JiBX, XStream]

* spring-ws-core-1.0-m2.jar
- Contents: The Spring-WS Core
- Dependencies: Commons Logging, spring-beans, spring-core, spring-context
                [Log4J, spring-oxm, spring-web, spring-webmvc, SAAJ, JDOM, StAX, Servlet API, JAF, Axiom, DOM4J, XOM]

* spring-ws-security-1.0-m2.jar
- Contents: Spring-WS Security integration
- Dependencies: Commons Logging, spring-beans, spring-core, spring-context, spring-ws-core
                [Log4J, xmlsdig, xmlsec, XWS-security, Acegi]

* spring-xml-1.0-m2.jar
- Contents: Spring XML utility framework
- Dependencies: Commons Logging, spring-beans, spring-core
                [StAX, Xalan, Jaxen]

For an exact list of Spring-WS project dependencies see the respective pom.xml files.

4. WHERE TO START

This distribution contains documentation and two sample applications illustrating the features of Spring-WS.

A great way to get started is to review and run the sample applications, supplementing with reference manual
material as needed. You will require Maven 2, which can be downloaded from http://maven.apache.org/, for building
Spring-WS. To build deployable .war files for all samples, simply access the "samples" directory and
execute the "mvn package" command.

More information on deploying Spring-WS sample applications can be found at:
	samples/readme.txt

5. ADDITIONAL RESOURCES

The Spring-WS homepage is located at:

    http://www.springframework.org/spring-ws

Spring-WS support forums are located at:

    http://forum.springframework.org/forumdisplay.php?f=39

The Spring Framework portal is located at:

	http://www.springframework.org
