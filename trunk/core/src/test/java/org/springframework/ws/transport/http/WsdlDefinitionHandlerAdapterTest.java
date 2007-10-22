/*
 * Copyright 2006 the original author or authors.
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import javax.servlet.ServletException;
import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.custommonkey.xmlunit.XMLTestCase;
import org.easymock.MockControl;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ws.wsdl.WsdlDefinition;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class WsdlDefinitionHandlerAdapterTest extends XMLTestCase {

    private WsdlDefinitionHandlerAdapter adapter;

    private MockControl definitionControl;

    private WsdlDefinition definitionMock;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    protected void setUp() throws Exception {
        adapter = new WsdlDefinitionHandlerAdapter();
        definitionControl = MockControl.createControl(WsdlDefinition.class);
        definitionMock = (WsdlDefinition) definitionControl.getMock();
        adapter.afterPropertiesSet();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    public void testHandleGet() throws Exception {
        request.setMethod("GET");
        String definition = "<definition xmlns='http://schemas.xmlsoap.org/wsdl/'/>";
        definitionControl.expectAndReturn(definitionMock.getSource(), new StringSource(definition));
        definitionControl.replay();
        adapter.handle(request, response, definitionMock);
        assertXMLEqual(definition, response.getContentAsString());
        definitionControl.verify();
    }

    public void testHandleNonGet() throws Exception {
        request.setMethod("POST");
        definitionControl.replay();
        try {
            adapter.handle(request, response, definitionMock);
            fail("ServletException expected");
        }
        catch (ServletException ex) {
            // expected
        }
        definitionControl.verify();
    }

    public void testTransformLocations() throws Exception {
        adapter.setTransformLocations(true);
        request.setMethod("GET");
        request.setScheme("http");
        request.setServerName("example.com");
        request.setServerPort(8080);
        request.setContextPath("/context");
        request.setServletPath("/service.wsdl");
        request.setPathInfo(null);
        request.setRequestURI("/context/service.wsdl");
        definitionControl.replay();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document result = documentBuilder.parse(getClass().getResourceAsStream("wsdl11-input.wsdl"));
        adapter.transformLocations(result, request);
        Document expectedDocument = documentBuilder.parse(getClass().getResourceAsStream("wsdl11-expected.wsdl"));
        assertXMLEqual("Invalid result", expectedDocument, result);

        definitionControl.verify();
    }

    public void testTransformLocationFullUrl() throws Exception {
        request.setScheme("http");
        request.setServerName("example.com");
        request.setServerPort(8080);
        request.setContextPath("/context");
        request.setPathInfo("/service.wsdl");
        request.setRequestURI("/context/service.wsdl");
        String oldLocation = "http://localhost:8080/context/service";

        String result = adapter.transformLocation(oldLocation, request);
        assertNotNull("No result", result);
        assertEquals("Invalid result", new URI("http://example.com:8080/context/service"), new URI(result));
    }

    public void testTransformLocationEmptyContextFullUrl() throws Exception {
        request.setScheme("http");
        request.setServerName("example.com");
        request.setServerPort(8080);
        request.setContextPath("");
        request.setRequestURI("/service.wsdl");
        String oldLocation = "http://localhost:8080/service";

        String result = adapter.transformLocation(oldLocation, request);
        assertNotNull("No result", result);
        assertEquals("Invalid result", new URI("http://example.com:8080/service"), new URI(result));
    }

    public void testTransformLocationRelativeUrl() throws Exception {
        request.setScheme("http");
        request.setServerName("example.com");
        request.setServerPort(8080);
        request.setContextPath("/context");
        request.setPathInfo("/service.wsdl");
        request.setRequestURI("/context/service.wsdl");
        String oldLocation = "/service";

        String result = adapter.transformLocation(oldLocation, request);
        assertNotNull("No result", result);
        assertEquals("Invalid result", new URI("http://example.com:8080/context/service"), new URI(result));
    }

    public void testTransformLocationEmptyContextRelativeUrl() throws Exception {
        request.setScheme("http");
        request.setServerName("example.com");
        request.setServerPort(8080);
        request.setContextPath("");
        request.setRequestURI("/service.wsdl");
        String oldLocation = "/service";

        String result = adapter.transformLocation(oldLocation, request);
        assertNotNull("No result", result);
        assertEquals("Invalid result", new URI("http://example.com:8080/service"), new URI(result));
    }

    public void testHandleSimpleWsdl11DefinitionWithoutTransformLocations() throws Exception {
        adapter.setTransformLocations(false);
        request.setMethod("GET");
        request.setScheme("http");
        request.setServerName("example.com");
        request.setServerPort(8080);
        request.setContextPath("/context");
        request.setServletPath("/service.wsdl");
        request.setPathInfo(null);
        request.setRequestURI("/context/service.wsdl");

        SimpleWsdl11Definition definition =
                new SimpleWsdl11Definition(new ClassPathResource("echo-input.wsdl", getClass()));

        adapter.handle(request, response, definition);
        InputStream inputStream = new ByteArrayInputStream(response.getContentAsByteArray());

        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLReader reader = factory.newWSDLReader();
        Definition wsdl4jDefinition = reader.readWSDL(null, new InputSource(inputStream));
        assertNotNull("No definition read", wsdl4jDefinition);

        inputStream = new ByteArrayInputStream(response.getContentAsByteArray());
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document resultingDocument = documentBuilder.parse(inputStream);

        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document expectedDocument = documentBuilder.parse(getClass().getResourceAsStream("echo-input.wsdl"));
        assertXMLEqual("Invalid WSDL returned", expectedDocument, resultingDocument);
    }

    public void testHandleSimpleWsdl11DefinitionWithTransformLocation() throws Exception {
        adapter.setTransformLocations(true);
        request.setMethod("GET");
        request.setScheme("http");
        request.setServerName("example.com");
        request.setServerPort(8080);
        request.setContextPath("/context");
        request.setServletPath("/service.wsdl");
        request.setPathInfo(null);
        request.setRequestURI("/context/service.wsdl");

        SimpleWsdl11Definition definition =
                new SimpleWsdl11Definition(new ClassPathResource("echo-input.wsdl", getClass()));

        adapter.handle(request, response, definition);
        InputStream inputStream = new ByteArrayInputStream(response.getContentAsByteArray());

        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLReader reader = factory.newWSDLReader();
        Definition wsdl4jDefinition = reader.readWSDL(null, new InputSource(inputStream));
        assertNotNull("No definition read", wsdl4jDefinition);

        inputStream = new ByteArrayInputStream(response.getContentAsByteArray());
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document resultingDocument = documentBuilder.parse(inputStream);

        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document expectedDocument = documentBuilder.parse(getClass().getResourceAsStream("echo-expected.wsdl"));
        assertXMLEqual("Invalid WSDL returned", expectedDocument, resultingDocument);
    }

}