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

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.TransportInputStream;
import org.springframework.ws.transport.TransportOutputStream;
import org.springframework.ws.transport.WebServiceConnection;

public abstract class AbstractHttpWebServiceMessageSenderIntegrationTestCase extends XMLTestCase {

    protected Server jettyServer;

    private static final String REQUEST_HEADER_NAME = "RequestHeader";

    private static final String REQUEST_HEADER_VALUE = "RequestHeaderValue";

    private static final String RESPONSE_HEADER_NAME = "ResponseHeader";

    private static final String RESPONSE_HEADER_VALUE = "ResponseHeaderValue";

    protected static final String REQUEST = "Request";

    protected static final String RESPONSE = "Response";

    protected AbstractHttpWebServiceMessageSender messageSender;

    private Context jettyContext;

    protected final void setUp() throws Exception {
        jettyServer = new Server(8888);
        jettyContext = new Context(jettyServer, "/");
        messageSender = createMessageSender();
        messageSender.setUrl(new URL("http://localhost:8888/"));
        XMLUnit.setIgnoreWhitespace(true);
    }

    protected abstract AbstractHttpWebServiceMessageSender createMessageSender();

    protected final void tearDown() throws Exception {
        if (jettyServer.isRunning()) {
            jettyServer.stop();
        }
    }

    public void testSendAndReceiveResponse() throws Exception {
        validateResponse(new ResponseServlet());
    }

    public void testSendAndReceiveNoResponse() throws Exception {
        validateNonResponse(new NoResponseServlet());
    }

    public void testSendAndReceiveNoResponseAccepted() throws Exception {
        NoResponseServlet servlet = new NoResponseServlet();
        servlet.setResponseStatus(HttpServletResponse.SC_ACCEPTED);
        validateNonResponse(servlet);
    }

    public void testSendAndReceiveCompressed() throws Exception {
        validateResponse(new CompressedResponseServlet());
    }

    public void testSendAndReceiveInvalidContentSize() throws Exception {
        validateResponse(new InvalidContentSizeServlet());
    }

    public void testSendAndReceiveFault() throws Exception {
        ResponseServlet servlet = new ResponseServlet();
        servlet.setResponseStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        jettyContext.addServlet(new ServletHolder(servlet), "/");
        jettyServer.start();
        FaultAwareWebServiceConnection connection = (FaultAwareWebServiceConnection) messageSender.createConnection();
        try {
            TransportOutputStream tos = connection.getTransportOutputStream();
            tos.addHeader("Content-Type", "text/xml");
            tos.addHeader(REQUEST_HEADER_NAME, REQUEST_HEADER_VALUE);
            FileCopyUtils.copy(REQUEST.getBytes("UTF-8"), tos);
            assertNotNull("No response", connection.getTransportInputStream());
            assertTrue("Response has no fault", connection.hasFault());
        }
        finally {
            connection.close();
        }
    }

    private void validateResponse(Servlet servlet) throws Exception {
        jettyContext.addServlet(new ServletHolder(servlet), "/");
        jettyServer.start();
        FaultAwareWebServiceConnection connection = (FaultAwareWebServiceConnection) messageSender.createConnection();
        try {
            TransportOutputStream tos = connection.getTransportOutputStream();
            tos.addHeader("Content-Type", "text/xml");
            tos.addHeader(REQUEST_HEADER_NAME, REQUEST_HEADER_VALUE);
            FileCopyUtils.copy(REQUEST.getBytes("UTF-8"), tos);
            assertNotNull("No response", connection.getTransportInputStream());
            assertFalse("Response has fault", connection.hasFault());
            TransportInputStream tis = connection.getTransportInputStream();
            boolean headerFound = false;
            for (Iterator iterator = tis.getHeaderNames(); iterator.hasNext();) {
                String headerName = (String) iterator.next();
                if (RESPONSE_HEADER_NAME.equals(headerName)) {
                    headerFound = true;
                }
            }
            assertTrue("Response has invalid header", headerFound);
            Iterator headerValues = tis.getHeaders(RESPONSE_HEADER_NAME);
            assertTrue("Response has no header values", headerValues.hasNext());
            assertEquals("Response has invalid header values", RESPONSE_HEADER_VALUE, headerValues.next());
            String result = new String(FileCopyUtils.copyToByteArray(tis), "UTF-8");
            assertEquals("Invalid response", RESPONSE, result);
        }
        finally {
            connection.close();
        }
    }

    private void validateNonResponse(Servlet servlet) throws Exception {
        jettyContext.addServlet(new ServletHolder(servlet), "/");
        jettyServer.start();

        WebServiceConnection connection = messageSender.createConnection();
        try {
            TransportOutputStream tos = connection.getTransportOutputStream();
            tos.addHeader(REQUEST_HEADER_NAME, REQUEST_HEADER_VALUE);
            FileCopyUtils.copy(REQUEST.getBytes("UTF-8"), tos);
            assertNull("Response", connection.getTransportInputStream());
        }
        finally {
            connection.close();
        }
    }

    private static class NoResponseServlet extends HttpServlet {

        protected int responseStatus = HttpServletResponse.SC_OK;

        public void setResponseStatus(int responseStatus) {
            this.responseStatus = responseStatus;
        }

        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            assertEquals("Invalid header value received on server side", REQUEST_HEADER_VALUE,
                    request.getHeader(REQUEST_HEADER_NAME));
            String receivedRequest = new String(FileCopyUtils.copyToByteArray(request.getInputStream()), "UTF-8");
            assertEquals("Invalid request received", REQUEST, receivedRequest);

            response.setStatus(responseStatus);
            createResponse(request, response);
        }

        protected void createResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        }
    }

    private static class ResponseServlet extends NoResponseServlet {

        protected void createResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
            response.setContentType("text/xml");
            response.addHeader(RESPONSE_HEADER_NAME, RESPONSE_HEADER_VALUE);
            byte[] buffer = RESPONSE.getBytes("UTF-8");
            response.setContentLength(buffer.length);
            FileCopyUtils.copy(buffer, response.getOutputStream());
        }
    }

    private static class InvalidContentSizeServlet extends NoResponseServlet {

        protected void createResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
            response.setContentType("text/xml");
            response.addHeader(RESPONSE_HEADER_NAME, RESPONSE_HEADER_VALUE);
            response.setContentLength(-1);
            byte[] buffer = RESPONSE.getBytes("UTF-8");
            FileCopyUtils.copy(buffer, response.getOutputStream());
        }
    }

    private static class CompressedResponseServlet extends NoResponseServlet {

        protected void createResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
            assertEquals("Invalid Accept-Encoding header value received on server side", "gzip",
                    request.getHeader("Accept-Encoding"));
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/xml");
            response.addHeader(RESPONSE_HEADER_NAME, RESPONSE_HEADER_VALUE);
            response.addHeader("Content-Encoding", "gzip");
            byte[] buffer = RESPONSE.getBytes("UTF-8");
            FileCopyUtils.copy(buffer, new GZIPOutputStream(response.getOutputStream()));
        }
    }
}
