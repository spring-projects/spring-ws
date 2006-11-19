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
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.springframework.util.FileCopyUtils;

public abstract class AbstractHttpWebServiceMessageSenderTestCase extends TestCase {

    protected Server jettyServer;

    protected static final String HEADER_NAME = "SOAPAction";

    protected static final String HEADER_VALUE = "http://springframework.org/spring-ws";

    protected static final String URL = "http://localhost:8888";

    protected final void setUp() throws Exception {
        onSetUp();
        jettyServer = new Server(8888);
        Context root = new Context(jettyServer, "/");
        root.addServlet(new ServletHolder(new EchoServlet()), "/*");
        jettyServer.start();
    }

    protected void onSetUp() throws Exception {

    }

    protected final void tearDown() throws Exception {
        jettyServer.stop();
    }

    private static class EchoServlet extends GenericServlet {

        public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
            HttpServletRequest httpServletRequest = (HttpServletRequest) req;
            HttpServletResponse httpServletResponse = (HttpServletResponse) res;
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
//            assertEquals("Invalid header", HEADER_VALUE, httpServletRequest.getHeader(HEADER_NAME));

            httpServletResponse.addHeader("Content-Type", "text/xml");
            httpServletResponse.addHeader(HEADER_NAME, HEADER_VALUE);
            FileCopyUtils.copy(req.getInputStream(), res.getOutputStream());
        }
    }
}
