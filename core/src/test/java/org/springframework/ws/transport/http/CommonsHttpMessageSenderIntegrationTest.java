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

package org.springframework.ws.transport.http;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MessageFactory;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.support.FreePortScanner;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.URIException;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

public class CommonsHttpMessageSenderIntegrationTest extends AbstractHttpWebServiceMessageSenderIntegrationTestCase {

    @Override
    protected AbstractHttpWebServiceMessageSender createMessageSender() {
        return new CommonsHttpMessageSender();
    }

    @Test(expected = ConnectTimeoutException.class)
    public void testConnectionTimeout() throws Exception {
        CommonsHttpMessageSender messageSender = new CommonsHttpMessageSender();
        messageSender.setConnectionTimeout(1);
        WebServiceConnection connection = messageSender.createConnection(new URI("http://example.com/"));
        WebServiceMessage message = new MockWebServiceMessage();
        connection.send(message);
    }

    @Test
    public void testMaxConnections() throws URISyntaxException, URIException {
        CommonsHttpMessageSender messageSender = new CommonsHttpMessageSender();
        messageSender.setMaxTotalConnections(2);
        Map<String, String> maxConnectionsPerHost = new HashMap<String, String>();
        maxConnectionsPerHost.put("https://www.example.com", "1");
        maxConnectionsPerHost.put("http://www.example.com:8080", "7");
        maxConnectionsPerHost.put("www.springframework.org", "10");
        maxConnectionsPerHost.put("*", "5");
        messageSender.setMaxConnectionsPerHost(maxConnectionsPerHost);
    }

    @Test
    public void testContextClose() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        int port = FreePortScanner.getFreePort();
        Server jettyServer = new Server(port);
        Context jettyContext = new Context(jettyServer, "/");
        jettyContext.addServlet(new ServletHolder(new EchoServlet()), "/");
        jettyServer.start();
        WebServiceConnection connection = null;
        try {

            StaticApplicationContext appContext = new StaticApplicationContext();
            appContext.registerSingleton("messageSender", CommonsHttpMessageSender.class);
            appContext.refresh();

            CommonsHttpMessageSender messageSender = appContext
                    .getBean("messageSender", CommonsHttpMessageSender.class);
            connection = messageSender.createConnection(new URI("http://localhost:" + port));

            appContext.close();

            connection.send(new SaajSoapMessage(messageFactory.createMessage()));
            connection.receive(new SaajSoapMessageFactory(messageFactory));
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
            if (jettyServer.isRunning()) {
                jettyServer.stop();
            }
        }

    }

    private class EchoServlet extends HttpServlet {

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            response.setContentType("text/xml");
            FileCopyUtils.copy(request.getInputStream(), response.getOutputStream());

        }
    }


}
