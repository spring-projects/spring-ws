SPRING WEB FLOW 1.0 (June 2006)
-------------------------------
http://www.springframework.org/webflow
http://forum.springframework.org

1. INTRODUCTION

Spring Web Flow (SWF) is a product of the Spring community focused on the definition
and execution of UI flow within a web application.

The system allows you to capture logical page flows as self-contained modules that are
reusable in different situations. The system is ideal for web applications that guide
the user through controlled navigations that drive business processes.  These processes
typically span HTTP requests and may be dynamic in nature.

SWF is a focused flow engine that integrates with existing frameworks like Spring MVC,
Struts, and JSF, capable of capturing your application's page flow explicity in a
declarative, reusable fashion.  SWF is a very a powerful framework based on a finite-state
machine for the definition and execution of web conversations.

2. RELEASE INFO

Spring Web Flow requires J2SE 1.3 and J2EE 1.3 (Servlet 2.3, JSP 1.2).  J2SE 1.5 is required for building.

SWF release contents:

"." contains Spring Web Flow distribution units (jars and source zip archives), readme, and copyright
"docs" contains the Spring Web Flow reference manual and API Javadocs
"projects" contains all buildable projects, including sample applications (each importable into Eclipse)
"projects/common-build" contains the Ant 1.6 "common build system" used by all projects to compile/build/test
"projects/repository" contains Spring Web Flow dependencies (dependent jars)
"projects/spring-webflow/build-spring-webflow" contains the master build file used to build all Spring Web Flow projects
"projects/spring-webflow/spring-binding" contains buildable Spring Data Binding project sources, an internal library used by SWF
"projects/spring-webflow/spring-webflow" contains buildable Spring Web Flow project sources
"projects/spring-webflow/spring-webflow-samples" contains buildable Spring Web Flow sample application sources

See the readme.txt within the above directories for additional information.

Spring Web Flow is released under the terms of the Apache Software License (see license.txt).

3. DISTRIBUTION JAR FILES

The following distinct jar files are included in the distribution. This list
specifies the respective contents and third-party dependencies. Libraries in [brackets] are
optional, i.e. just necessary for certain functionality.

* spring-webflow-1.0.jar
- Contents: The Spring Web Flow system
- Dependencies: Commons Logging, spring-beans, spring-core, spring-context, spring-web, spring-binding,
                [Log4J, Commons Codec, OGNL, spring-webmvc, spring-mock, JUnit, Servlet API, Portlet API, JMX, Struts, JSF]
              
* spring-binding-1.0.jar
- Contents: The Spring Data Binding framework, an internal library used by SWF
- Dependencies: Commons Logging, spring-beans, spring-core, spring-context
                [Log4J]

For an exact list of Spring Web Flow project dependencies see "projects/spring-webflow/ivy.xml".

4. WHERE TO START

This distribution contains extensive documentation and sample applications illustrating the
features of Spring Web Flow.

*** A great way to get started is to review and run the sample applications, supplimenting with
reference manual material as needed.  To build deployable .war files for all samples, simply 
access the "build-spring-webflow" directory and execute the "dist" target. ***

More information on deploying SWF sample applications can be found at:
	projects/spring-webflow/spring-webflow-samples/readme.txt
	
5. ADDITIONAL RESOURCES

The Spring Web Flow homepage is located at:

	http://www.springframework.org/webflow

There you will find resources such as a 'Quick Start' guide and a 'Frequently Asked Questions'
section.

Spring Web Flow support forums are located at:

	http://forum.springframework.org
	
There you will find an active community supporting the use of the product.

The Spring Framework portal is located at:

	http://www.springframework.org

There you will find links to many resources related to the Spring Framework, including on-line access 
to Spring and Spring Web Flow documentation.