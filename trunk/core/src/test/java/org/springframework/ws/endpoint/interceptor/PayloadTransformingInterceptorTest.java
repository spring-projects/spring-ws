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

package org.springframework.ws.endpoint.interceptor;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.transform.StringResult;

public class PayloadTransformingInterceptorTest extends XMLTestCase {

    private PayloadTransformingInterceptor interceptor;

    private Transformer transformer;

    private Resource input;

    private Resource output;

    private Resource xslt;

    protected void setUp() throws Exception {
        interceptor = new PayloadTransformingInterceptor();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        input = new ClassPathResource("transformInput.xml", getClass());
        output = new ClassPathResource("transformOutput.xml", getClass());
        xslt = new ClassPathResource("transformation.xslt", getClass());
        XMLUnit.setIgnoreWhitespace(true);
    }

    public void testHandleRequest() throws Exception {
        interceptor.setRequestXslt(xslt);
        interceptor.afterPropertiesSet();
        MockWebServiceMessage request = new MockWebServiceMessage(input);
        MessageContext context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

        boolean result = interceptor.handleRequest(context, null);
        assertTrue("Invalid interceptor result", result);
        StringResult stringResult = new StringResult();
        transformer.transform(new SAXSource(SaxUtils.createInputSource(output)), stringResult);
        assertXMLEqual(stringResult.toString(), request.getPayloadAsString());
    }

    public void testHandleRequestNoXslt() throws Exception {
        interceptor.setResponseXslt(xslt);
        interceptor.afterPropertiesSet();
        MockWebServiceMessage request = new MockWebServiceMessage(input);
        MessageContext context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

        boolean result = interceptor.handleRequest(context, null);
        assertTrue("Invalid interceptor result", result);
        StringResult stringResult = new StringResult();
        transformer.transform(new SAXSource(SaxUtils.createInputSource(input)), stringResult);
        assertXMLEqual(stringResult.toString(), request.getPayloadAsString());
    }

    public void testHandleResponse() throws Exception {
        interceptor.setResponseXslt(xslt);
        interceptor.afterPropertiesSet();
        MockWebServiceMessage request = new MockWebServiceMessage(input);
        MessageContext context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
        MockWebServiceMessage response = (MockWebServiceMessage) context.getResponse();
        response.setPayload(input);

        boolean result = interceptor.handleResponse(context, null);
        assertTrue("Invalid interceptor result", result);
        StringResult stringResult = new StringResult();
        transformer.transform(new SAXSource(SaxUtils.createInputSource(output)), stringResult);
        assertXMLEqual(stringResult.toString(), response.getPayloadAsString());
    }

    public void testHandleResponseNoXslt() throws Exception {
        interceptor.setRequestXslt(xslt);
        interceptor.afterPropertiesSet();
        MockWebServiceMessage request = new MockWebServiceMessage(input);
        MessageContext context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
        MockWebServiceMessage response = (MockWebServiceMessage) context.getResponse();
        response.setPayload(input);

        boolean result = interceptor.handleResponse(context, null);
        assertTrue("Invalid interceptor result", result);
        StringResult stringResult = new StringResult();
        transformer.transform(new SAXSource(SaxUtils.createInputSource(input)), stringResult);
        assertXMLEqual(stringResult.toString(), response.getPayloadAsString());
    }

    public void testNoStylesheetsSet() throws Exception {
        try {
            interceptor.afterPropertiesSet();
            fail("Should have thrown an Exception");
        }
        catch (IllegalArgumentException ex) {
        }
    }
}