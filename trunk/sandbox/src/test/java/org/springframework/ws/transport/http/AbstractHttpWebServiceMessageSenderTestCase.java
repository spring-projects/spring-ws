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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public abstract class AbstractHttpWebServiceMessageSenderTestCase extends XMLTestCase {

    protected Server jettyServer;

    protected static final String HEADER_NAME = "SOAPAction";

    protected static final String HEADER_VALUE = "http://springframework.org/spring-ws";

    protected static final String URL = "http://localhost:8888";

    protected static final String REQUEST = "<Request xmlns='http://springframework.org/spring-ws'/>";

    protected static final String EXPECTED_SOAP_REQUEST =
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'>" + "<SOAP-ENV:Header/>" +
                    "<SOAP-ENV:Body>" + REQUEST + "</SOAP-ENV:Body>" + "</SOAP-ENV:Envelope>";

    protected static final String RESPONSE = "<Response xmlns='http://springframework.org/spring-ws'/>";

    protected Transformer transformer;

    protected AbstractHttpWebServiceMessageSender messageSender;

    private MessageFactory messageFactory;

    private String receivedRequest;

    private String receivedHeader;

    protected final void setUp() throws Exception {
        messageFactory = MessageFactory.newInstance();
        transformer = TransformerFactory.newInstance().newTransformer();
        jettyServer = new Server(8888);
        Context root = new Context(jettyServer, "/");
        root.addServlet(new ServletHolder(new ResponseServlet()), "/response");
        root.addServlet(new ServletHolder(new NoResponseServlet()), "/noresponse");
        jettyServer.start();
        messageSender = createMessageSender();
        XMLUnit.setIgnoreWhitespace(true);
    }

    protected abstract AbstractHttpWebServiceMessageSender createMessageSender();

    protected final void tearDown() throws Exception {
        jettyServer.stop();
    }

    public void testSendAndReceiveResponse() throws Exception {
        messageSender.setUrl(new URL("http://localhost:8888/response"));
        SOAPMessage saajRequest = messageFactory.createMessage();
        saajRequest.getMimeHeaders().addHeader(HEADER_NAME, HEADER_VALUE);
        transformer.transform(new StringSource(REQUEST), new DOMResult(saajRequest.getSOAPBody()));
        SaajSoapMessage request = new SaajSoapMessage(saajRequest);
        MessageContext context = new DefaultMessageContext(request, new SaajSoapMessageFactory(messageFactory));
        messageSender.sendAndReceive(context);
        assertXMLEqual(EXPECTED_SOAP_REQUEST, receivedRequest.toString());
        assertEquals("Invalid header value received", HEADER_VALUE, receivedHeader);
        assertTrue("No response", context.hasResponse());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        context.getResponse().writeTo(os);
        assertXMLEqual(RESPONSE, os.toString("UTF-8"));
    }

    public void testSendAndReceiveNoResponse() throws Exception {
        messageSender.setUrl(new URL("http://localhost:8888/noresponse"));
        SOAPMessage saajRequest = messageFactory.createMessage();
        transformer.transform(new StringSource(REQUEST), new DOMResult(saajRequest.getSOAPBody()));
        SaajSoapMessage request = new SaajSoapMessage(saajRequest);
        MessageContext context = new DefaultMessageContext(request, new SaajSoapMessageFactory(messageFactory));
        messageSender.sendAndReceive(context);
        assertFalse("Response", context.hasResponse());
    }

    private class ResponseServlet extends GenericServlet {

        public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
            try {
                StringResult requestResult = new StringResult();
                transformer.transform(new StreamSource(req.getInputStream()), requestResult);
                receivedRequest = requestResult.toString();
                receivedHeader = ((HttpServletRequest) req).getHeader(HEADER_NAME);

                HttpServletResponse httpServletResponse = (HttpServletResponse) res;
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                httpServletResponse.addHeader("Content-Type", "text/xml");
                FileCopyUtils.copy(RESPONSE.getBytes("UTF-8"), res.getOutputStream());
            }
            catch (TransformerException ex) {
                throw new ServletException(ex);
            }
        }
    }

    private class NoResponseServlet extends GenericServlet {

        public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
            try {
                StringResult requestResult = new StringResult();
                transformer.transform(new StreamSource(req.getInputStream()), requestResult);
                receivedRequest = requestResult.toString();
                receivedHeader = ((HttpServletRequest) req).getHeader(HEADER_NAME);

                HttpServletResponse httpServletResponse = (HttpServletResponse) res;
                httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
            catch (TransformerException ex) {
                throw new ServletException(ex);
            }
        }
    }
}
