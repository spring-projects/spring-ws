/*
 * Copyright ${YEAR} the original author or authors.
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

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.ws.transport.TransportConstants;

public class WebServiceHttpHandlerIntegrationTest extends AbstractDependencyInjectionSpringContextTests {

    private HttpClient client;

    protected String[] getConfigLocations() {
        return new String[]{"classpath:org/springframework/ws/transport/http/httpserver-applicationContext.xml"};
    }

    protected void onSetUp() throws Exception {
        client = new HttpClient();
    }

    public void testInvalidMethod() throws IOException {
        GetMethod getMethod = new GetMethod("http://localhost:8888/service");
        client.executeMethod(getMethod);
        assertEquals("Invalid Response Code", HttpTransportConstants.STATUS_METHOD_NOT_ALLOWED,
                getMethod.getStatusCode());
        assertEquals("Response retrieved", 0, getMethod.getResponseContentLength());
    }

    public void testNoResponse() throws IOException {
        PostMethod postMethod = new PostMethod("http://localhost:8888/service");
        postMethod.addRequestHeader(HttpTransportConstants.HEADER_CONTENT_TYPE, "text/xml");
        postMethod.addRequestHeader(TransportConstants.HEADER_SOAP_ACTION,
                "http://springframework.org/spring-ws/NoResponse");
        Resource soapRequest = new ClassPathResource("soapRequest.xml", WebServiceHttpHandlerIntegrationTest.class);
        postMethod.setRequestEntity(new InputStreamRequestEntity(soapRequest.getInputStream()));
        client.executeMethod(postMethod);
        assertEquals("Invalid Response Code", HttpTransportConstants.STATUS_ACCEPTED, postMethod.getStatusCode());
        assertEquals("Response retrieved", 0, postMethod.getResponseContentLength());
    }

    public void testResponse() throws IOException {
        PostMethod postMethod = new PostMethod("http://localhost:8888/service");
        postMethod.addRequestHeader(HttpTransportConstants.HEADER_CONTENT_TYPE, "text/xml");
        postMethod.addRequestHeader(TransportConstants.HEADER_SOAP_ACTION,
                "http://springframework.org/spring-ws/Response");
        Resource soapRequest = new ClassPathResource("soapRequest.xml", WebServiceHttpHandlerIntegrationTest.class);
        postMethod.setRequestEntity(new InputStreamRequestEntity(soapRequest.getInputStream()));
        client.executeMethod(postMethod);
        assertEquals("Invalid Response Code", HttpTransportConstants.STATUS_OK, postMethod.getStatusCode());
        assertTrue("No Response retrieved", postMethod.getResponseContentLength() > 0);
    }

    public void testNoEndpoint() throws IOException {
        PostMethod postMethod = new PostMethod("http://localhost:8888/service");
        postMethod.addRequestHeader(HttpTransportConstants.HEADER_CONTENT_TYPE, "text/xml");
        postMethod.addRequestHeader(TransportConstants.HEADER_SOAP_ACTION,
                "http://springframework.org/spring-ws/NoEndpoint");
        Resource soapRequest = new ClassPathResource("soapRequest.xml", WebServiceHttpHandlerIntegrationTest.class);
        postMethod.setRequestEntity(new InputStreamRequestEntity(soapRequest.getInputStream()));
        client.executeMethod(postMethod);
        assertEquals("Invalid Response Code", HttpTransportConstants.STATUS_NOT_FOUND, postMethod.getStatusCode());
        assertEquals("Response retrieved", 0, postMethod.getResponseContentLength());
    }

    public void testFault() throws IOException {
        PostMethod postMethod = new PostMethod("http://localhost:8888/service");
        postMethod.addRequestHeader(HttpTransportConstants.HEADER_CONTENT_TYPE, "text/xml");
        postMethod
                .addRequestHeader(TransportConstants.HEADER_SOAP_ACTION, "http://springframework.org/spring-ws/Fault");
        Resource soapRequest = new ClassPathResource("soapRequest.xml", WebServiceHttpHandlerIntegrationTest.class);
        postMethod.setRequestEntity(new InputStreamRequestEntity(soapRequest.getInputStream()));
        client.executeMethod(postMethod);
        assertEquals("Invalid Response Code", HttpTransportConstants.STATUS_INTERNAL_SERVER_ERROR,
                postMethod.getStatusCode());
        assertTrue("No Response retrieved", postMethod.getResponseContentLength() > 0);
    }

}