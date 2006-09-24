SPRING WEB SERVICES 1.0-M1 (June 2006)
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

Spring-WS requires J2SE 1.4 and J2EE 1.4 (for now).  J2SE 1.5 is required for building.

Release contents:

"." contains Spring-WS distribution units (jars and source zip archives), readme, and copyright
"docs" contains the Spring-WS reference manual and API Javadocs
"projects" contains all buildable projects, including sample applications
"projects/build-spring-ws" contains the master build file used to build all projects
"projects/common-build" contains the Ant 1.6 "common build system" used by all projects to compile/build/test
"projects/repository" contains Spring-WS dependencies (dependent jars)
"projects/spring-oxm" contains buildable Spring O/X Mapping sources, an internal library used by Spring-WS
"projects/spring-ws-core" contains buildable Spring-WS core sources
"projects/spring-ws-security" contains buildable Spring-WS security sources
"projects/spring-ws-samples" contains buildable Spring-WS sample application sources

See the readme.txt within the above directories for additional information.

Spring-WS is released under the terms of the Apache Software License (see license.txt).

3. DISTRIBUTION JAR FILES

The following distinct jar files are included in the distribution. This list specifies the respective contents and
third-party dependencies. Libraries in [brackets] are optional, i.e. just necessary for certain functionality.

* spring-ws-core-1.0-m1.jar
- Contents: The Spring-WS Core
- Dependencies: Commons Logging, spring-beans, spring-core, spring-context
                [Log4J, spring-oxm, spring-web, spring-webmvc, SAAJ, JDOM, StAX, Servlet API, JAF, Axiom, DOM4J, XOM]

* spring-ws-security-1.0-m1.jar
- Contents: Spring-WS Security implementation that integrates with Acegi
- Dependencies: Commons Logging, spring-beans, spring-core, spring-context, spring-ws-core
                [Log4J, xmlsdig, xmlsec, XWS-security, Acegi]

* spring-oxm-1.0-m1.jar
- Contents: The Spring Object/XML Mapping framework
- Dependencies: Commons Logging, spring-beans, spring-core
                [Log4J, JAXB, Castor, XMLBeans, StAX, JiBX, XStream]

For an exact list of Spring-WS project dependencies see the respective ivy.xml files.

4. WHERE TO START

This distribution contains documentation and a sample application illustrating the features of Spring-WS.

A great way to get started is to review and run the sample applications, supplementing with reference manual
material as needed. To build deployable .war files for all samples, simply access the "build-spring-ws" directory and
execute the "dist" target.

More information on deploying Spring-WS sample applications can be found at:
	projects/spring-ws-samples/readme.txt

5. ADDITIONAL RESOURCES

The Spring-WS homepage is located at:

    http://www.springframework.org/spring-ws

Spring-WS support forums are located at:

    http://forum.springframework.org/forumdisplay.php?f=39

The Spring Framework portal is located at:

	http://www.springframework.org
