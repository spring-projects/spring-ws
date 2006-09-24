/*
 * spring-webflow-samples
 *
 * birthdate - demonstrates Spring Web Flow Struts 1.1 or > integration
 * fileupload - demonstrates multipart file upload
 * flowlauncher - demonstrates the different ways to launch flows from web pages
 * itemlist - demonstrates application transactions and inline flows
 * numberguess - demonstrates how to play a game using a flow
 * phonebook - central sample demonstrating most features
 * phonebook-portlet - the phonebook sample in a portlet environment (notice how the flow definitions do not change)
 * sellitem - demonstrates a wizard with conditional transitions and continuations
 * sellitem-jsf - the sellitem sample in a jsf environment (notice how the flow definition does not change)
 * shippingrate - demonstrates Spring Web Flow together with Ajax technology.
 */

Sample pre-requisites:
----------------------
* JDK 1.4+ must be installed with the JAVA_HOME variable set.  The sellitem sample requires JDK 5.0.

* Ant 1.6 must be installed and in your system path

* A Servlet 2.4 and JSP 2.0-capable servlet container must be installed for sample app deployment
    - The samples all use jsp 2.0 to take advantage of ${expressions} for elegance.

To build all samples:
---------------------
1. cd to the ../build-spring-webflow directory

2. run 'ant dist' to produce deployable .war files for all samples.
   Built .war files are placed in target/artifacts/war within each sample directory.

To build an individual sample:
---------------------
1. cd to the sample root directory

2. run 'ant dist' to produce a deployable .war file within target/artifacts/war.