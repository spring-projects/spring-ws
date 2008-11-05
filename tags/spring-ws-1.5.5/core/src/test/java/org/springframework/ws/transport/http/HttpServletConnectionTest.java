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

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.custommonkey.xmlunit.XMLTestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public class HttpServletConnectionTest extends XMLTestCase {

    private HttpServletConnection connection;

    private MockHttpServletRequest httpServletRequest;

    private MockHttpServletResponse httpServletResponse;

    private static final String HEADER_NAME = "RequestHeader";

    private static final String HEADER_VALUE = "RequestHeaderValue";

    private static final String CONTENT = "<Request xmlns='http://springframework.org/spring-ws/' />";

    private static final String SOAP_CONTENT =
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'><SOAP-ENV:Header/><SOAP-ENV:Body>" +
                    CONTENT + "</SOAP-ENV:Body></SOAP-ENV:Envelope>";

    private SaajSoapMessageFactory messageFactory;

    private TransformerFactory transformerFactory;

    protected void setUp() throws Exception {
        httpServletRequest = new MockHttpServletRequest();
        httpServletResponse = new MockHttpServletResponse();
        connection = new HttpServletConnection(httpServletRequest, httpServletResponse);
        MessageFactory saajMessageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        messageFactory = new SaajSoapMessageFactory(saajMessageFactory);
        transformerFactory = TransformerFactory.newInstance();
    }

    public void testReceive() throws Exception {
        byte[] bytes = SOAP_CONTENT.getBytes("UTF-8");
        httpServletRequest.addHeader("Content-Type", "text/xml");
        httpServletRequest.addHeader("Content-Length", new Integer(bytes.length).toString());
        httpServletRequest.addHeader(HEADER_NAME, HEADER_VALUE);
        httpServletRequest.setContent(bytes);
        SaajSoapMessage message = (SaajSoapMessage) connection.receive(messageFactory);
        assertNotNull("No message received", message);
        StringResult result = new StringResult();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(message.getPayloadSource(), result);
        assertXMLEqual("Invalid message", CONTENT, result.toString());
        SOAPMessage saajMessage = message.getSaajMessage();
        String[] headerValues = saajMessage.getMimeHeaders().getHeader(HEADER_NAME);
        assertNotNull("Response has no header", headerValues);
        assertEquals("Response has invalid header", 1, headerValues.length);
        assertEquals("Response has invalid header values", HEADER_VALUE, headerValues[0]);
    }

    public void testSend() throws Exception {
        SaajSoapMessage message = (SaajSoapMessage) messageFactory.createWebServiceMessage();
        SOAPMessage saajMessage = message.getSaajMessage();
        MimeHeaders mimeHeaders = saajMessage.getMimeHeaders();
        mimeHeaders.addHeader(HEADER_NAME, HEADER_VALUE);
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(new StringSource(CONTENT), message.getPayloadResult());

        connection.send(message);

        assertEquals("Invalid header", HEADER_VALUE, httpServletResponse.getHeader(HEADER_NAME));
        assertXMLEqual("Invalid content", SOAP_CONTENT, httpServletResponse.getContentAsString());
    }

}