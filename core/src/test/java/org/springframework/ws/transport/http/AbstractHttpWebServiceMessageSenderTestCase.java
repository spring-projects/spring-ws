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
import javax.servlet.GenericServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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

public abstract class AbstractHttpWebServiceMessageSenderTestCase extends XMLTestCase {

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
        validateResponse(new MyServlet(true));
    }

    public void testSendAndReceiveResponseInvalidContentLength() throws Exception {
        validateResponse(new MyServlet(true, HttpServletResponse.SC_OK, false));
    }

    public void testSendAndReceiveFault() throws Exception {
        jettyContext
                .addServlet(new ServletHolder(new MyServlet(true, HttpServletResponse.SC_INTERNAL_SERVER_ERROR)), "/");
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

    public void testSendAndReceiveNoResponse() throws Exception {
        validateNonResponse(new MyServlet(false));
    }

    public void testSendAndReceiveNoResponseAccepted() throws Exception {
        validateNonResponse(new MyServlet(false, HttpServletResponse.SC_ACCEPTED));
    }

    public void testSendAndReceiveNoResponseInvalidContentLength() throws Exception {
        validateNonResponse(new MyServlet(false, HttpServletResponse.SC_OK, false));
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

    private static class MyServlet extends GenericServlet {

        private boolean response;

        private int responseStatus;

        private boolean validContentLength;

        public MyServlet(boolean response) {
            this(response, HttpServletResponse.SC_OK, true);
        }

        public MyServlet(boolean response, int responseStatus) {
            this(response, responseStatus, true);
        }

        public MyServlet(boolean response, int responseStatus, boolean validContentLength) {
            this.response = response;
            this.responseStatus = responseStatus;
            this.validContentLength = validContentLength;
        }

        public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
            HttpServletRequest httpServletRequest = (HttpServletRequest) req;
            HttpServletResponse httpServletResponse = (HttpServletResponse) res;
            assertEquals("Invalid header value received on server side", REQUEST_HEADER_VALUE,
                    httpServletRequest.getHeader(REQUEST_HEADER_NAME));
            String receivedRequest = new String(FileCopyUtils.copyToByteArray(req.getInputStream()), "UTF-8");
            assertEquals("Invalid request received", REQUEST, receivedRequest);

            httpServletResponse.setStatus(responseStatus);
            if (!validContentLength) {
                httpServletResponse.setContentLength(-1);
            }
            if (response) {
                httpServletResponse.addHeader(RESPONSE_HEADER_NAME, RESPONSE_HEADER_VALUE);
                httpServletResponse.setContentType("text/xml");
                FileCopyUtils.copy(RESPONSE.getBytes("UTF-8"), res.getOutputStream());
            }
        }
    }
}
