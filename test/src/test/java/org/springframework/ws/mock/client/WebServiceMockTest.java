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

package org.springframework.ws.mock.client;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.ws.mock.client.WebServiceMock.*;

public class WebServiceMockTest {

    private WebServiceTemplate template;

    @Before
    public void setUp() throws Exception {
        template = new WebServiceTemplate();
        template.setDefaultUri("http://example.com");

        mockWebServiceTemplate(template);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void mocks() throws Exception {
        URI uri = URI.create("http://example.com");

        RequestMatcher requestMatcher1 = EasyMock.createStrictMock("requestMatcher1", RequestMatcher.class);
        RequestMatcher requestMatcher2 = EasyMock.createStrictMock("requestMatcher2", RequestMatcher.class);
        ResponseCreator responseCreator = EasyMock.createStrictMock(ResponseCreator.class);

        SaajSoapMessage response = new SaajSoapMessageFactory(MessageFactory.newInstance()).createWebServiceMessage();

        requestMatcher1.match(EasyMock.eq(uri), EasyMock.isA(SaajSoapMessage.class));
        requestMatcher2.match(EasyMock.eq(uri), EasyMock.isA(SaajSoapMessage.class));
        EasyMock.expect(responseCreator.createResponse(EasyMock.eq(uri), EasyMock.isA(SaajSoapMessage.class),
                EasyMock.isA(SaajSoapMessageFactory.class))).andReturn(response);

        EasyMock.replay(requestMatcher1, requestMatcher2, responseCreator);

        expect(requestMatcher1).andExpect(requestMatcher2).andRespond(responseCreator);
        template.sendSourceAndReceiveToResult(uri.toString(), new StringSource("<request xmlns='http://example.com'/>"),
                new StringResult());

        EasyMock.verify(requestMatcher1, requestMatcher2, responseCreator);
    }

    @Test
    public void payloadMatch() throws Exception {
        Source request = new StringSource("<request xmlns='http://example.com'/>");
        Source response = new StringSource("<response xmlns='http://example.com'/>");

        expect(payload(request)).andRespond(withPayload(response));

        StringResult result = new StringResult();
        template.sendSourceAndReceiveToResult(request, result);
        assertXMLEqual(result.toString(), response.toString());
    }

    @Test(expected = AssertionError.class)
    public void payloadNonMatch() throws Exception {
        Source expected = new StringSource("<request xmlns='http://example.com'/>");

        expect(payload(expected));

        StringResult result = new StringResult();
        String actual = "<request xmlns='http://other.com'/>";
        template.sendSourceAndReceiveToResult(new StringSource(actual), result);
    }

    @Test
    public void soapHeaderMatch() throws Exception {
        final QName soapHeaderName = new QName("http://example.com", "mySoapHeader");

        expect(soapHeader(soapHeaderName));

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

        expect(soapHeader(soapHeaderName));

        template.sendSourceAndReceiveToResult(new StringSource("<request xmlns='http://example.com'/>"),
                new StringResult());
    }

    @Test
    public void connectionMatch() throws Exception {
        String uri = "http://example.com";
        expect(connectionTo(uri));

        template.sendSourceAndReceiveToResult(uri, new StringSource("<request xmlns='http://example.com'/>"),
                new StringResult());
    }

    @Test(expected = AssertionError.class)
    public void connectionNonMatch() throws Exception {
        String expected = "http://expected.com";
        expect(connectionTo(expected));

        String actual = "http://actual.com";
        template.sendSourceAndReceiveToResult(actual, new StringSource("<request xmlns='http://example.com'/>"),
                new StringResult());
    }

    @Test
    public void verifyThreadLocalCleanUp() throws Exception {
        Source request = new StringSource("<request xmlns='http://example.com'/>");
        Source response = new StringSource("<response xmlns='http://example.com'/>");

        expect(payload(request)).andRespond(withPayload(response));
        expect(payload(request)).andRespond(withPayload(response));
        assertNotNull(MockWebServiceMessageSenderHolder.get());

        template.sendSourceAndReceiveToResult(request, new StringResult());
        assertNotNull(MockWebServiceMessageSenderHolder.get());

        template.sendSourceAndReceiveToResult(request, new StringResult());
        assertNull(MockWebServiceMessageSenderHolder.get());
    }

    @Test(expected = AssertionError.class)
    public void unexpectedConnection() throws Exception {
        Source request = new StringSource("<request xmlns='http://example.com'/>");
        Source response = new StringSource("<response xmlns='http://example.com'/>");

        expect(payload(request)).andRespond(withPayload(response));

        template.sendSourceAndReceiveToResult(request, new StringResult());
        template.sendSourceAndReceiveToResult(request, new StringResult());
    }

    @Test
    public void xsdMatch() throws Exception {
        Resource schema = new ByteArrayResource(
                "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://example.com\" elementFormDefault=\"qualified\"><element name=\"request\"/></schema>".getBytes());

        expect(validPayload(schema));

        StringResult result = new StringResult();
        String actual = "<request xmlns='http://example.com'/>";
        template.sendSourceAndReceiveToResult(new StringSource(actual), result);
    }

    @Test(expected = AssertionError.class)
    public void xsdNonMatch() throws Exception {
        Resource schema = new ByteArrayResource(
                "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://example.com\" elementFormDefault=\"qualified\"><element name=\"request\"/></schema>".getBytes());

        expect(validPayload(schema));

        StringResult result = new StringResult();
        String actual = "<request2 xmlns='http://example.com'/>";
        template.sendSourceAndReceiveToResult(new StringSource(actual), result);
    }

    @Test
    public void xpathExistsMatch() throws Exception {
        final Map<String, String> ns = Collections.singletonMap("ns", "http://example.com");

        expect(xpath("/ns:request", ns).exists());

        template.sendSourceAndReceiveToResult(new StringSource("<request xmlns='http://example.com'/>"),
                new StringResult());
    }

    @Test(expected = AssertionError.class)
    public void xpathExistsNonMatch() throws Exception {
        final Map<String, String> ns = Collections.singletonMap("ns", "http://example.com");

        expect(xpath("/ns:foo", ns).exists());

        template.sendSourceAndReceiveToResult(new StringSource("<request xmlns='http://example.com'/>"),
                new StringResult());
    }

    @Test
    public void anythingMatch() throws Exception {
        Source request = new StringSource("<request xmlns='http://example.com'/>");
        Source response = new StringSource("<response xmlns='http://example.com'/>");

        expect(anything()).andRespond(withPayload(response));

        StringResult result = new StringResult();
        template.sendSourceAndReceiveToResult(request, result);
        assertXMLEqual(result.toString(), response.toString());

        verifyConnections();
    }

    @Test(expected = IllegalStateException.class)
    public void recordWhenReplay() throws Exception {
        Source request = new StringSource("<request xmlns='http://example.com'/>");
        Source response = new StringSource("<response xmlns='http://example.com'/>");

        expect(anything()).andRespond(withPayload(response));
        expect(anything()).andRespond(withPayload(response));

        StringResult result = new StringResult();
        template.sendSourceAndReceiveToResult(request, result);
        assertXMLEqual(result.toString(), response.toString());

        expect(anything()).andRespond(withPayload(response));
    }

    @Test(expected = AssertionError.class)
    public void verifyFailure() throws Exception {
        expect(anything());
        verifyConnections();
    }

    @Test
    public void verifyOnly() throws Exception {
        verifyConnections();
    }
}
