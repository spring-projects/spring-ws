/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.http;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.transport.TransportConstants;

public class MessageDispatcherServletIntegrationTest extends TestCase {

    private Server jettyServer;

    private HttpClient client;

    private static final String CONTENT_TYPE = "Content-Type";

    protected final void setUp() throws Exception {
        jettyServer = new Server(8888);
        Context jettyContext = new Context(jettyServer, "/");
        File dir = new File(getClass().getResource(".").toURI());
        jettyContext.setResourceBase(dir.getAbsolutePath());
        ServletHolder servletHolder = new ServletHolder(new MessageDispatcherServlet());
        servletHolder.setName("spring-ws");
        jettyContext.addServlet(servletHolder, "/*");
        jettyServer.start();
        client = new HttpClient();
    }

    protected void tearDown() throws Exception {
        jettyServer.stop();
    }

    public void testNoResponse() throws IOException {
        PostMethod postMethod = new PostMethod("http://localhost:8888/service");
        postMethod.addRequestHeader(CONTENT_TYPE, "text/xml");
        postMethod.addRequestHeader(TransportConstants.HEADER_SOAP_ACTION,
                "http://springframework.org/spring-ws/NoResponse");
        Resource soapRequest = new ClassPathResource("soapRequest.xml", MessageDispatcherServletIntegrationTest.class);
        postMethod.setRequestEntity(new InputStreamRequestEntity(soapRequest.getInputStream()));
        client.executeMethod(postMethod);
        assertEquals("Invalid Response Code", HttpStatus.SC_ACCEPTED, postMethod.getStatusCode());
        assertEquals("Response retrieved", 0, postMethod.getResponseContentLength());
    }

    public void testResponse() throws IOException {
        PostMethod postMethod = new PostMethod("http://localhost:8888/service");
        postMethod.addRequestHeader(CONTENT_TYPE, "text/xml");
        postMethod.addRequestHeader(TransportConstants.HEADER_SOAP_ACTION,
                "http://springframework.org/spring-ws/Response");
        Resource soapRequest = new ClassPathResource("soapRequest.xml", MessageDispatcherServletIntegrationTest.class);
        postMethod.setRequestEntity(new InputStreamRequestEntity(soapRequest.getInputStream()));
        client.executeMethod(postMethod);
        assertEquals("Invalid Response Code", HttpStatus.SC_OK, postMethod.getStatusCode());
        assertTrue("No Response retrieved", postMethod.getResponseContentLength() > 0);
    }

    public void testNoEndpoint() throws IOException {
        PostMethod postMethod = new PostMethod("http://localhost:8888/service");
        postMethod.addRequestHeader(CONTENT_TYPE, "text/xml");
        postMethod.addRequestHeader(TransportConstants.HEADER_SOAP_ACTION,
                "http://springframework.org/spring-ws/NoEndpoint");
        Resource soapRequest = new ClassPathResource("soapRequest.xml", MessageDispatcherServletIntegrationTest.class);
        postMethod.setRequestEntity(new InputStreamRequestEntity(soapRequest.getInputStream()));
        client.executeMethod(postMethod);
        assertEquals("Invalid Response Code", HttpStatus.SC_NOT_FOUND, postMethod.getStatusCode());
        assertEquals("Response retrieved", 0, postMethod.getResponseContentLength());
    }

    public void testFault() throws IOException {
        PostMethod postMethod = new PostMethod("http://localhost:8888/service");
        postMethod.addRequestHeader(CONTENT_TYPE, "text/xml");
        postMethod
                .addRequestHeader(TransportConstants.HEADER_SOAP_ACTION, "http://springframework.org/spring-ws/Fault");
        Resource soapRequest = new ClassPathResource("soapRequest.xml", MessageDispatcherServletIntegrationTest.class);
        postMethod.setRequestEntity(new InputStreamRequestEntity(soapRequest.getInputStream()));
        client.executeMethod(postMethod);
        assertEquals("Invalid Response Code", HttpStatus.SC_INTERNAL_SERVER_ERROR, postMethod.getStatusCode());
        assertTrue("No Response retrieved", postMethod.getResponseContentLength() > 0);
    }


}
