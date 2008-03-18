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

package org.springframework.ws.client.core;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.XMLTestCase;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.springframework.util.StringUtils;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.pox.dom.DomPoxMessageFactory;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.http.CommonsHttpMessageSender;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public class WebServiceTemplateIntegrationTest extends XMLTestCase {

    private WebServiceTemplate template;

    private Server jettyServer;

    protected void setUp() throws Exception {
        jettyServer = new Server(8888);
        Context jettyContext = new Context(jettyServer, "/");
        jettyContext.addServlet(new ServletHolder(new EchoSoapServlet()), "/soap/echo");
        jettyContext.addServlet(new ServletHolder(new SoapFaultServlet()), "/soap/fault");
        SoapFaultServlet badRequestFault = new SoapFaultServlet();
        badRequestFault.setSc(400);
        jettyContext.addServlet(new ServletHolder(badRequestFault), "/soap/badRequestFault");
        jettyContext.addServlet(new ServletHolder(new NoResponseSoapServlet()), "/soap/noResponse");
        jettyContext.addServlet(new ServletHolder(new PoxServlet()), "/pox");
        jettyContext.addServlet(new ServletHolder(new ErrorServlet(404)), "/errors/notfound");
        jettyContext.addServlet(new ServletHolder(new ErrorServlet(500)), "/errors/server");
        jettyServer.start();
    }

    protected void tearDown() throws Exception {
        jettyServer.stop();
    }

    public void testAxiom() throws Exception {
        testSoap(new AxiomSoapMessageFactory());
    }

    public void testWithSaaj() throws Exception {
        testSoap(new SaajSoapMessageFactory(MessageFactory.newInstance()));
    }

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
            fail("WebServiceTransportException expected");
        }
        catch (WebServiceTransportException ex) {
            //expected
        }
        try {
            template.sendSourceAndReceiveToResult("http://localhost:8888/errors/server", new StringSource(content),
                    result);
            fail("WebServiceTransportException expected");
        }
        catch (WebServiceTransportException ex) {
            //expected
        }
    }

    private void testSoap(SoapMessageFactory messageFactory)
            throws SAXException, IOException, ParserConfigurationException {
        template = new WebServiceTemplate(messageFactory);
        template.setMessageSender(new CommonsHttpMessageSender());
        String content = "<root xmlns='http://springframework.org/spring-ws'><child/></root>";
        StringResult result = new StringResult();
        template.sendSourceAndReceiveToResult("http://localhost:8888/soap/echo", new StringSource(content), result);
        assertXMLEqual(content, result.toString());
        boolean b = template.sendSourceAndReceiveToResult("http://localhost:8888/soap/noResponse",
                new StringSource(content), new StringResult());
        assertFalse("Invalid result", b);
        try {
            template.sendSourceAndReceiveToResult("http://localhost:8888/errors/notfound", new StringSource(content),
                    new StringResult());
            fail("WebServiceTransportException expected");
        }
        catch (WebServiceTransportException ex) {
            //expected
        }
        try {
            template.sendSourceAndReceiveToResult("http://localhost:8888/soap/fault", new StringSource(content),
                    result);
            fail("SoapFaultClientException expected");
        }
        catch (SoapFaultClientException ex) {
            //expected
        }
        template.setCheckConnectionForFault(false);
        try {
            template.sendSourceAndReceiveToResult("http://localhost:8888/soap/badRequestFault",
                    new StringSource(content), result);
            fail("SoapFaultClientException expected");
        }
        catch (SoapFaultClientException ex) {
            //expected
        }
        template.setCheckConnectionForFault(true);
    }

    /** Servlet that returns and error message for a given status code. */
    private static class ErrorServlet extends HttpServlet {

        private int sc;

        private ErrorServlet(int sc) {
            this.sc = sc;
        }

        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.sendError(sc);
        }
    }

    /** Simple POX Servlet. */
    private static class PoxServlet extends HttpServlet {

        private DocumentBuilderFactory documentBuilderFactory;

        private TransformerFactory transformerFactory;

        public void init(ServletConfig servletConfig) throws ServletException {
            super.init(servletConfig);
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            transformerFactory = TransformerFactory.newInstance();
        }

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

        protected MessageFactory msgFactory = null;

        private int sc = -1;

        public void setSc(int sc) {
            this.sc = sc;
        }

        public void init(ServletConfig servletConfig) throws ServletException {
            super.init(servletConfig);
            try {
                msgFactory = MessageFactory.newInstance();
            }
            catch (SOAPException ex) {
                throw new ServletException("Unable to create message factory" + ex.getMessage());
            }
        }

        public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            try {
                MimeHeaders headers = getHeaders(req);
                SOAPMessage request = msgFactory.createMessage(headers, req.getInputStream());
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
                throw new ServletException("SAAJ POST failed " + ex.getMessage());
            }
        }

        private MimeHeaders getHeaders(HttpServletRequest httpServletRequest) {
            Enumeration enumeration = httpServletRequest.getHeaderNames();
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
            Iterator it = headers.getAllHeaders();
            while (it.hasNext()) {
                MimeHeader header = (MimeHeader) it.next();
                String[] values = headers.getHeader(header.getName());
                res.setHeader(header.getName(), StringUtils.arrayToCommaDelimitedString(values));
            }
        }

        protected abstract SOAPMessage onMessage(SOAPMessage message) throws SOAPException;
    }

    private static class EchoSoapServlet extends AbstractSoapServlet {

        protected SOAPMessage onMessage(SOAPMessage message) throws SOAPException {
            return message;
        }
    }

    private static class NoResponseSoapServlet extends AbstractSoapServlet {

        protected SOAPMessage onMessage(SOAPMessage message) throws SOAPException {
            return null;
        }
    }

    private static class SoapFaultServlet extends AbstractSoapServlet {

        protected SOAPMessage onMessage(SOAPMessage message) throws SOAPException {
            SOAPMessage response = msgFactory.createMessage();
            SOAPBody body = response.getSOAPBody();
            body.addFault(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Server"), "Server fault");
            return response;
        }
    }

}
