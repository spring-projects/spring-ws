/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.test.client;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import org.junit.Before;
import org.junit.Test;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNotNull;
import static org.springframework.ws.test.client.RequestMatchers.*;
import static org.springframework.ws.test.client.ResponseCreators.withClientOrSenderFault;
import static org.springframework.ws.test.client.ResponseCreators.withPayload;

public class MockWebServiceServerTest {

    private WebServiceTemplate template;

    private MockWebServiceServer server;

    @Before
    public void setUp() throws Exception {
        template = new WebServiceTemplate();
        template.setDefaultUri("http://example.com");

        server = MockWebServiceServer.createServer(template);
    }

    @Test
    public void createServerWebServiceTemplate() throws Exception {
        WebServiceTemplate template = new WebServiceTemplate();

        MockWebServiceServer server = MockWebServiceServer.createServer(template);
        assertNotNull(server);
    }

    @Test
    public void createServerGatewaySupport() throws Exception {
        MyClient client = new MyClient();

        MockWebServiceServer server = MockWebServiceServer.createServer(client);
        assertNotNull(server);
    }

    @Test
    public void createServerApplicationContextWebServiceTemplate() throws Exception {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("webServiceTemplate", WebServiceTemplate.class);
        applicationContext.refresh();

        MockWebServiceServer server = MockWebServiceServer.createServer(applicationContext);
        assertNotNull(server);
    }

    @Test
    public void createServerApplicationContextWebServiceGatewaySupport() throws Exception {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("myClient", MyClient.class);
        applicationContext.refresh();

        MockWebServiceServer server = MockWebServiceServer.createServer(applicationContext);
        assertNotNull(server);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createServerApplicationContextEmpty() throws Exception {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        MockWebServiceServer server = MockWebServiceServer.createServer(applicationContext);
        assertNotNull(server);
    }

    @Test
    public void mocks() throws Exception {
        URI uri = URI.create("http://example.com");

        RequestMatcher requestMatcher1 = createStrictMock("requestMatcher1", RequestMatcher.class);
        RequestMatcher requestMatcher2 = createStrictMock("requestMatcher2", RequestMatcher.class);
        ResponseCreator responseCreator = createStrictMock(ResponseCreator.class);

        SaajSoapMessage response = new SaajSoapMessageFactory(MessageFactory.newInstance()).createWebServiceMessage();

        requestMatcher1.match(eq(uri), isA(SaajSoapMessage.class));
        requestMatcher2.match(eq(uri), isA(SaajSoapMessage.class));
        expect(responseCreator.createResponse(eq(uri), isA(SaajSoapMessage.class), isA(SaajSoapMessageFactory.class)))
                .andReturn(response);

        replay(requestMatcher1, requestMatcher2, responseCreator);

        server.expect(requestMatcher1).andExpect(requestMatcher2).andRespond(responseCreator);
        template.sendSourceAndReceiveToResult(uri.toString(), new StringSource("<request xmlns='http://example.com'/>"),
                new StringResult());

        verify(requestMatcher1, requestMatcher2, responseCreator);
    }

    @Test
    public void payloadMatch() throws Exception {
        Source request = new StringSource("<request xmlns='http://example.com'/>");
        Source response = new StringSource("<response xmlns='http://example.com'/>");

        server.expect(payload(request)).andRespond(withPayload(response));

        StringResult result = new StringResult();
        template.sendSourceAndReceiveToResult(request, result);
        assertXMLEqual(result.toString(), response.toString());
    }

    @Test(expected = AssertionError.class)
    public void payloadNonMatch() throws Exception {
        Source expected = new StringSource("<request xmlns='http://example.com'/>");

        server.expect(payload(expected));

        StringResult result = new StringResult();
        String actual = "<request xmlns='http://other.com'/>";
        template.sendSourceAndReceiveToResult(new StringSource(actual), result);
    }

    @Test
    public void soapHeaderMatch() throws Exception {
        final QName soapHeaderName = new QName("http://example.com", "mySoapHeader");

        server.expect(soapHeader(soapHeaderName));

        template.sendSourceAndReceiveToResult(new StringSource("<request xmlns='http://example.com'/>"),
                new WebServiceMessageCallback() {
                    public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {
                        SoapMessage soapMessage = (SoapMessage) message;
                        soapMessage.getSoapHeader().addHeaderElement(soapHeaderName);
                    }
                }, new StringResult());
    }

    @Test(expected = AssertionError.class)
    public void soapHeaderNonMatch() throws Exception {
        QName soapHeaderName = new QName("http://example.com", "mySoapHeader");

        server.expect(soapHeader(soapHeaderName));

        template.sendSourceAndReceiveToResult(new StringSource("<request xmlns='http://example.com'/>"),
                new StringResult());
    }

    @Test
    public void connectionMatch() throws Exception {
        String uri = "http://example.com";
        server.expect(connectionTo(uri));

        template.sendSourceAndReceiveToResult(uri, new StringSource("<request xmlns='http://example.com'/>"),
                new StringResult());
    }

    @Test(expected = AssertionError.class)
    public void connectionNonMatch() throws Exception {
        String expected = "http://expected.com";
        server.expect(connectionTo(expected));

        String actual = "http://actual.com";
        template.sendSourceAndReceiveToResult(actual, new StringSource("<request xmlns='http://example.com'/>"),
                new StringResult());
    }

    @Test(expected = AssertionError.class)
    public void unexpectedConnection() throws Exception {
        Source request = new StringSource("<request xmlns='http://example.com'/>");
        Source response = new StringSource("<response xmlns='http://example.com'/>");

        server.expect(payload(request)).andRespond(withPayload(response));

        template.sendSourceAndReceiveToResult(request, new StringResult());
        template.sendSourceAndReceiveToResult(request, new StringResult());
    }

    @Test
    public void xsdMatch() throws Exception {
        Resource schema = new ByteArrayResource(
                "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://example.com\" elementFormDefault=\"qualified\"><element name=\"request\"/></schema>".getBytes());

        server.expect(validPayload(schema));

        StringResult result = new StringResult();
        String actual = "<request xmlns='http://example.com'/>";
        template.sendSourceAndReceiveToResult(new StringSource(actual), result);
    }

    @Test(expected = AssertionError.class)
    public void xsdNonMatch() throws Exception {
        Resource schema = new ByteArrayResource(
                "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://example.com\" elementFormDefault=\"qualified\"><element name=\"request\"/></schema>".getBytes());

        server.expect(validPayload(schema));

        StringResult result = new StringResult();
        String actual = "<request2 xmlns='http://example.com'/>";
        template.sendSourceAndReceiveToResult(new StringSource(actual), result);
    }

    @Test
    public void xpathExistsMatch() throws Exception {
        final Map<String, String> ns = Collections.singletonMap("ns", "http://example.com");

        server.expect(xpath("/ns:request", ns).exists());

        template.sendSourceAndReceiveToResult(new StringSource("<request xmlns='http://example.com'/>"),
                new StringResult());
    }

    @Test(expected = AssertionError.class)
    public void xpathExistsNonMatch() throws Exception {
        final Map<String, String> ns = Collections.singletonMap("ns", "http://example.com");

        server.expect(xpath("/ns:foo", ns).exists());

        template.sendSourceAndReceiveToResult(new StringSource("<request xmlns='http://example.com'/>"),
                new StringResult());
    }

    @Test
    public void anythingMatch() throws Exception {
        Source request = new StringSource("<request xmlns='http://example.com'/>");
        Source response = new StringSource("<response xmlns='http://example.com'/>");

        server.expect(anything()).andRespond(withPayload(response));

        StringResult result = new StringResult();
        template.sendSourceAndReceiveToResult(request, result);
        assertXMLEqual(result.toString(), response.toString());

        server.verify();
    }

    @Test(expected = IllegalStateException.class)
    public void recordWhenReplay() throws Exception {
        Source request = new StringSource("<request xmlns='http://example.com'/>");
        Source response = new StringSource("<response xmlns='http://example.com'/>");

        server.expect(anything()).andRespond(withPayload(response));
        server.expect(anything()).andRespond(withPayload(response));

        StringResult result = new StringResult();
        template.sendSourceAndReceiveToResult(request, result);
        assertXMLEqual(result.toString(), response.toString());

        server.expect(anything()).andRespond(withPayload(response));
    }

    @Test(expected = AssertionError.class)
    public void verifyFailure() throws Exception {
        server.expect(anything());
        server.verify();
    }

    @Test
    public void verifyOnly() throws Exception {
        server.verify();
    }

    @Test(expected = SoapFaultClientException.class)
    public void fault() throws Exception {
        Source request = new StringSource("<request xmlns='http://example.com'/>");

        server.expect(anything()).andRespond(withClientOrSenderFault("reason", Locale.ENGLISH));

        StringResult result = new StringResult();
        template.sendSourceAndReceiveToResult(request, result);
    }
       
    public static class MyClient extends WebServiceGatewaySupport {

    }
}
