/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.server.endpoint.interceptor;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.pox.dom.DomPoxMessage;
import org.springframework.ws.pox.dom.DomPoxMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.transform.ResourceSource;
import org.springframework.xml.transform.StringResult;

public class PayloadTransformingInterceptorTest {

    private PayloadTransformingInterceptor interceptor;

    private Transformer transformer;

    private Resource input;

    private Resource output;

    private Resource xslt;

    @Before
    public void setUp() throws Exception {
        interceptor = new PayloadTransformingInterceptor();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        input = new ClassPathResource("transformInput.xml", getClass());
        output = new ClassPathResource("transformOutput.xml", getClass());
        xslt = new ClassPathResource("transformation.xslt", getClass());
        XMLUnit.setIgnoreWhitespace(true);
    }

    @Test
    public void testHandleRequest() throws Exception {
        interceptor.setRequestXslt(xslt);
        interceptor.afterPropertiesSet();
        MockWebServiceMessage request = new MockWebServiceMessage(input);
        MessageContext context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

        boolean result = interceptor.handleRequest(context, null);
        Assert.assertTrue("Invalid interceptor result", result);
        StringResult expected = new StringResult();
        transformer.transform(new SAXSource(SaxUtils.createInputSource(output)), expected);
        assertXMLEqual(expected.toString(), request.getPayloadAsString());
    }

    @Test
    public void testHandleRequestNoXslt() throws Exception {
        interceptor.setResponseXslt(xslt);
        interceptor.afterPropertiesSet();
        MockWebServiceMessage request = new MockWebServiceMessage(input);
        MessageContext context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

        boolean result = interceptor.handleRequest(context, null);
        Assert.assertTrue("Invalid interceptor result", result);
        StringResult expected = new StringResult();
        transformer.transform(new SAXSource(SaxUtils.createInputSource(input)), expected);
        assertXMLEqual(expected.toString(), request.getPayloadAsString());
    }

    @Test
    public void testHandleResponse() throws Exception {
        interceptor.setResponseXslt(xslt);
        interceptor.afterPropertiesSet();
        MockWebServiceMessage request = new MockWebServiceMessage(input);
        MessageContext context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
        MockWebServiceMessage response = (MockWebServiceMessage) context.getResponse();
        response.setPayload(input);

        boolean result = interceptor.handleResponse(context, null);
        Assert.assertTrue("Invalid interceptor result", result);
        StringResult expected = new StringResult();
        transformer.transform(new SAXSource(SaxUtils.createInputSource(output)), expected);
        assertXMLEqual(expected.toString(), response.getPayloadAsString());
    }

    @Test
    public void testHandleResponseNoXslt() throws Exception {
        interceptor.setRequestXslt(xslt);
        interceptor.afterPropertiesSet();
        MockWebServiceMessage request = new MockWebServiceMessage(input);
        MessageContext context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
        MockWebServiceMessage response = (MockWebServiceMessage) context.getResponse();
        response.setPayload(input);

        boolean result = interceptor.handleResponse(context, null);
        Assert.assertTrue("Invalid interceptor result", result);
        StringResult expected = new StringResult();
        transformer.transform(new SAXSource(SaxUtils.createInputSource(input)), expected);
        assertXMLEqual(expected.toString(), response.getPayloadAsString());
    }

    @Test
    public void testSaaj() throws Exception {
        interceptor.setRequestXslt(xslt);
        interceptor.afterPropertiesSet();
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage saajMessage = messageFactory.createMessage();
        SaajSoapMessage message = new SaajSoapMessage(saajMessage);
        transformer.transform(new ResourceSource(input), message.getPayloadResult());
        MessageContext context = new DefaultMessageContext(message, new SaajSoapMessageFactory(messageFactory));

        Assert.assertTrue("Invalid interceptor result", interceptor.handleRequest(context, null));
        StringResult expected = new StringResult();
        transformer.transform(new SAXSource(SaxUtils.createInputSource(output)), expected);
        StringResult result = new StringResult();
        transformer.transform(message.getPayloadSource(), result);
        assertXMLEqual(expected.toString(), result.toString());

    }

    @Test
    public void testPox() throws Exception {
        interceptor.setRequestXslt(xslt);
        interceptor.afterPropertiesSet();
        DomPoxMessageFactory factory = new DomPoxMessageFactory();
        DomPoxMessage message = factory.createWebServiceMessage();
        transformer.transform(new ResourceSource(input), message.getPayloadResult());
        MessageContext context = new DefaultMessageContext(message, factory);

        Assert.assertTrue("Invalid interceptor result", interceptor.handleRequest(context, null));
        StringResult expected = new StringResult();
        transformer.transform(new SAXSource(SaxUtils.createInputSource(output)), expected);
        StringResult result = new StringResult();
        transformer.transform(message.getPayloadSource(), result);
        assertXMLEqual(expected.toString(), result.toString());

    }

    @Test
    public void testNoStylesheetsSet() throws Exception {
        try {
            interceptor.afterPropertiesSet();
            Assert.fail("Should have thrown an Exception");
        }
        catch (IllegalArgumentException ex) {
        }
    }
}