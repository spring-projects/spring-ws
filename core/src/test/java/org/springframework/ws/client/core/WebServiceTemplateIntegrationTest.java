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

package org.springframework.ws.client.core;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.MailcapCommandMap;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.pox.dom.DomPoxMessageFactory;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.http.CommonsHttpMessageSender;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;

public class WebServiceTemplateIntegrationTest {

    private static Server jettyServer;

    private WebServiceTemplate template;


    private String messagePayload = "<root xmlns='http://springframework.org/spring-ws'><child/></root>";

    @BeforeClass
    public static void startJetty() throws Exception {
        jettyServer = new Server(8888);
        Context jettyContext = new Context(jettyServer, "/");
        jettyContext.addServlet(new ServletHolder(new EchoSoapServlet()), "/soap/echo");
        jettyContext.addServlet(new ServletHolder(new SoapFaultServlet()), "/soap/fault");
        SoapFaultServlet badRequestFault = new SoapFaultServlet();
        badRequestFault.setSc(400);
        jettyContext.addServlet(new ServletHolder(badRequestFault), "/soap/badRequestFault");
        jettyContext.addServlet(new ServletHolder(new NoResponseSoapServlet()), "/soap/noResponse");
        jettyContext.addServlet(new ServletHolder(new AttachmentsServlet()), "/soap/attachment");
        jettyContext.addServlet(new ServletHolder(new PoxServlet()), "/pox");
        jettyContext.addServlet(new ServletHolder(new ErrorServlet(404)), "/errors/notfound");
        jettyContext.addServlet(new ServletHolder(new ErrorServlet(500)), "/errors/server");
        jettyServer.start();
    }
    
    /**
     * A workaround for the faulty XmlDataContentHandler in the SAAJ RI, which cannot handle mime types such as
     * "text/xml; charset=UTF-8", causing issues with Axiom. We basically reset the command map
     */
    @Before
    public void removeXmlDataContentHandler() throws SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage message = messageFactory.createMessage();
        message.createAttachmentPart();
        CommandMap.setDefaultCommandMap(new MailcapCommandMap());
    }
    
    @AfterClass
    public static void stopJetty() throws Exception {
        if (jettyServer.isRunning()) {
            jettyServer.stop();            
        }
    }
    
    @Test
    public void testSaaj() throws Exception {
        doSoap(new SaajSoapMessageFactory(MessageFactory.newInstance()));
    }

    @Test
    public void testAxiom() throws Exception {
        doSoap(new AxiomSoapMessageFactory());
    }

    @Test
    public void testAxiomNonCaching() throws Exception {
        AxiomSoapMessageFactory axiomFactory = new AxiomSoapMessageFactory();
        axiomFactory.setPayloadCaching(false);
        doSoap(axiomFactory);
    }

    @Test
    public void testPox() throws Exception {
        template = new WebServiceTemplate(new DomPoxMessageFactory());
        template.setMessageSender(new CommonsHttpMessageSender());
        String content = "<root xmlns='http://springframework.org/spring-ws'><child/></root>";
        StringResult result = new StringResult();
        template.sendSourceAndReceiveToResult("http://localhost:8888/pox", new StringSource(content), result);
        assertXMLEqual(content, result.toString());
        try {
            template.sendSourceAndReceiveToResult("http://localhost:8888/errors/notfound", new StringSource(content),
                    new StringResult());
            Assert.fail("WebServiceTransportException expected");
        }
        catch (WebServiceTransportException ex) {
            //expected
        }
        try {
            template.sendSourceAndReceiveToResult("http://localhost:8888/errors/server", new StringSource(content),
                    result);
            Assert.fail("WebServiceTransportException expected");
        }
        catch (WebServiceTransportException ex) {
            //expected
        }
    }

    private void doSoap(SoapMessageFactory messageFactory) throws Exception {
        template = new WebServiceTemplate(messageFactory);
        template.setMessageSender(new CommonsHttpMessageSender());
        sendSourceAndReceiveToResult();
        sendSourceAndReceiveToResultNoResponse();
        marshalSendAndReceiveResponse();
        marshalSendAndReceiveNoResponse();
        notFound();
        fault();
        faultNonCompliant();
        attachment();
    }

    private void sendSourceAndReceiveToResult() throws SAXException, IOException {
        StringResult result = new StringResult();
        boolean b = template.sendSourceAndReceiveToResult("http://localhost:8888/soap/echo",
                new StringSource(messagePayload), result);
        Assert.assertTrue("Invalid result", b);
        assertXMLEqual(messagePayload, result.toString());
    }

    private void sendSourceAndReceiveToResultNoResponse() {
        boolean b = template.sendSourceAndReceiveToResult("http://localhost:8888/soap/noResponse",
                new StringSource(messagePayload), new StringResult());
        Assert.assertFalse("Invalid result", b);
    }

    private void marshalSendAndReceiveResponse() throws TransformerConfigurationException {
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        final Object requestObject = new Object();
        Marshaller marshaller = new Marshaller() {

            public void marshal(Object graph, Result result) throws XmlMappingException, IOException {
                Assert.assertEquals("Invalid object", graph, requestObject);
                try {
                    transformer.transform(new StringSource(messagePayload), result);
                }
                catch (TransformerException e) {
                    Assert.fail(e.getMessage());
                }
            }

            public boolean supports(Class<?> clazz) {
                Assert.assertEquals("Invalid class", Object.class, clazz);
                return true;
            }
        };
        final Object responseObject = new Object();
        Unmarshaller unmarshaller = new Unmarshaller() {

            public Object unmarshal(Source source) throws XmlMappingException, IOException {
                return responseObject;
            }

            public boolean supports(Class<?> clazz) {
                Assert.assertEquals("Invalid class", Object.class, clazz);
                return true;
            }
        };
        template.setMarshaller(marshaller);
        template.setUnmarshaller(unmarshaller);
        Object result = template.marshalSendAndReceive("http://localhost:8888/soap/echo", requestObject);
        Assert.assertEquals("Invalid response object", responseObject, result);
    }

    private void marshalSendAndReceiveNoResponse() throws TransformerConfigurationException {
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        final Object requestObject = new Object();
        Marshaller marshaller = new Marshaller() {

            public void marshal(Object graph, Result result) throws XmlMappingException, IOException {
                Assert.assertEquals("Invalid object", graph, requestObject);
                try {
                    transformer.transform(new StringSource(messagePayload), result);
                }
                catch (TransformerException e) {
                    Assert.fail(e.getMessage());
                }
            }

            public boolean supports(Class<?> clazz) {
                Assert.assertEquals("Invalid class", Object.class, clazz);
                return true;
            }
        };
        template.setMarshaller(marshaller);
        Object result = template.marshalSendAndReceive("http://localhost:8888/soap/noResponse", requestObject);
        Assert.assertNull("Invalid response object", result);
    }

    private void notFound() {
        try {
            template.sendSourceAndReceiveToResult("http://localhost:8888/errors/notfound",
                    new StringSource(messagePayload), new StringResult());
            Assert.fail("WebServiceTransportException expected");
        }
        catch (WebServiceTransportException ex) {
            //expected
        }
    }

    private void fault() {
        Result result = new StringResult();
        try {
            template.sendSourceAndReceiveToResult("http://localhost:8888/soap/fault", new StringSource(messagePayload),
                    result);
            Assert.fail("SoapFaultClientException expected");
        }
        catch (SoapFaultClientException ex) {
            //expected
        }
    }

    private void faultNonCompliant() {
        Result result = new StringResult();
        template.setCheckConnectionForFault(false);
        template.setCheckConnectionForError(false);
        try {
            template.sendSourceAndReceiveToResult("http://localhost:8888/soap/badRequestFault",
                    new StringSource(messagePayload), result);
            Assert.fail("SoapFaultClientException expected");
        }
        catch (SoapFaultClientException ex) {
            //expected
        }
    }

    private void attachment() {
        template.sendSourceAndReceiveToResult("http://localhost:8888/soap/attachment", new StringSource(messagePayload),
                new WebServiceMessageCallback() {

                    public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {
                        SoapMessage soapMessage = (SoapMessage) message;
                        final String attachmentContent = "content";
                        soapMessage.addAttachment("attachment-1",
                                new DataHandler(new ByteArrayDataSource(attachmentContent, "text/plain")));
                    }
                }, new StringResult());
    }

    /** Servlet that returns and error message for a given status code. */
    private static class ErrorServlet extends HttpServlet {

        private int sc;

        private ErrorServlet(int sc) {
            this.sc = sc;
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.sendError(sc);
        }
    }

    /** Simple POX Servlet. */
    private static class PoxServlet extends HttpServlet {

        private DocumentBuilderFactory documentBuilderFactory;

        private TransformerFactory transformerFactory;

        @Override
        public void init(ServletConfig servletConfig) throws ServletException {
            super.init(servletConfig);
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            transformerFactory = TransformerFactory.newInstance();
        }

        @Override
        public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            try {
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document message = documentBuilder.parse(req.getInputStream());
                Transformer transformer = transformerFactory.newTransformer();
                transformer.transform(new DOMSource(message), new StreamResult(resp.getOutputStream()));
            }
            catch (Exception ex) {
                throw new ServletException("POX POST failed" + ex.getMessage());
            }
        }
    }

    /** Abstract SOAP Servlet */
    private abstract static class AbstractSoapServlet extends HttpServlet {

        protected MessageFactory messageFactory = null;

        private int sc = -1;

        public void setSc(int sc) {
            this.sc = sc;
        }

        @Override
        public void init(ServletConfig servletConfig) throws ServletException {
            super.init(servletConfig);
            try {
                messageFactory = MessageFactory.newInstance();
            }
            catch (SOAPException ex) {
                throw new ServletException("Unable to create message factory" + ex.getMessage());
            }
        }

        @Override
        public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            try {
                MimeHeaders headers = getHeaders(req);
                SOAPMessage request = messageFactory.createMessage(headers, req.getInputStream());
                SOAPMessage reply = onMessage(request);
                if (sc != -1) {
                    resp.setStatus(sc);
                }
                if (reply != null) {
                    if (reply.saveRequired()) {
                        reply.saveChanges();
                    }
                    if (sc == -1) {
                        resp.setStatus(!reply.getSOAPBody().hasFault() ? HttpServletResponse.SC_OK :
                                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                    putHeaders(reply.getMimeHeaders(), resp);
                    reply.writeTo(resp.getOutputStream());
                }
                else if (sc == -1) {
                    resp.setStatus(HttpServletResponse.SC_ACCEPTED);
                }
            }
            catch (Exception ex) {
                throw new ServletException("SAAJ POST failed " + ex.getMessage(), ex);
            }
        }

        private MimeHeaders getHeaders(HttpServletRequest httpServletRequest) {
            Enumeration<?> enumeration = httpServletRequest.getHeaderNames();
            MimeHeaders headers = new MimeHeaders();
            while (enumeration.hasMoreElements()) {
                String headerName = (String) enumeration.nextElement();
                String headerValue = httpServletRequest.getHeader(headerName);
                StringTokenizer values = new StringTokenizer(headerValue, ",");
                while (values.hasMoreTokens()) {
                    headers.addHeader(headerName, values.nextToken().trim());
                }
            }
            return headers;
        }

        private void putHeaders(MimeHeaders headers, HttpServletResponse res) {
            Iterator<?> it = headers.getAllHeaders();
            while (it.hasNext()) {
                MimeHeader header = (MimeHeader) it.next();
                String[] values = headers.getHeader(header.getName());
                res.setHeader(header.getName(), StringUtils.arrayToCommaDelimitedString(values));
            }
        }

        protected abstract SOAPMessage onMessage(SOAPMessage message) throws SOAPException;
    }

    private static class EchoSoapServlet extends AbstractSoapServlet {

        @Override
        protected SOAPMessage onMessage(SOAPMessage message) throws SOAPException {
            return message;
        }
    }

    private static class NoResponseSoapServlet extends AbstractSoapServlet {

        @Override
        protected SOAPMessage onMessage(SOAPMessage message) throws SOAPException {
            return null;
        }
    }

    private static class SoapFaultServlet extends AbstractSoapServlet {

        @Override
        protected SOAPMessage onMessage(SOAPMessage message) throws SOAPException {
            SOAPMessage response = messageFactory.createMessage();
            SOAPBody body = response.getSOAPBody();
            body.addFault(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Server"), "Server fault");
            return response;
        }
    }

    private static class AttachmentsServlet extends AbstractSoapServlet {

        @Override
        protected SOAPMessage onMessage(SOAPMessage message) throws SOAPException {
            assertEquals("No attachments found", 1, message.countAttachments());
            return null;
        }
    }

}
