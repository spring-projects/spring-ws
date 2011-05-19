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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MessageFactory;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.URIException;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.WebServiceConnection;

public class CommonsHttpMessageSenderIntegrationTest extends AbstractHttpWebServiceMessageSenderIntegrationTestCase {

    protected AbstractHttpWebServiceMessageSender createMessageSender() {
        return new CommonsHttpMessageSender();
    }

    public void testConnectionTimeout() throws Exception {
        CommonsHttpMessageSender messageSender = new CommonsHttpMessageSender();
        messageSender.setConnectionTimeout(1);
        WebServiceConnection connection = messageSender.createConnection(new URI("http://example.com/"));
        WebServiceMessage message = new MockWebServiceMessage();
        try {
            connection.send(message);
            fail("ConnectTimeoutException expected");
        }
        catch (ConnectTimeoutException ex) {
            // expected
        }
    }

    public void testMaxConnections() throws URISyntaxException, URIException {
        CommonsHttpMessageSender messageSender = new CommonsHttpMessageSender();
        messageSender.setMaxTotalConnections(2);
        Properties maxConnectionsPerHost = new Properties();
        maxConnectionsPerHost.setProperty("https://www.example.com", "1");
        maxConnectionsPerHost.setProperty("http://www.example.com:8080", "7");
        maxConnectionsPerHost.setProperty("www.springframework.org", "10");
        maxConnectionsPerHost.setProperty("*", "5");
        messageSender.setMaxConnectionsPerHost(maxConnectionsPerHost);
    }

    public void testContextClose() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        Server jettyServer = new Server(8888);
        Context jettyContext = new Context(jettyServer, "/");
        jettyContext.addServlet(new ServletHolder(new EchoServlet()), "/");
        jettyServer.start();
        WebServiceConnection connection = null;
        try {

            StaticApplicationContext appContext = new StaticApplicationContext();
            appContext.registerSingleton("messageSender", CommonsHttpMessageSender.class);
            appContext.refresh();

            CommonsHttpMessageSender messageSender = (CommonsHttpMessageSender) appContext
                    .getBean("messageSender", CommonsHttpMessageSender.class);
            connection = messageSender.createConnection(new URI("http://localhost:8888/"));

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

        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            response.setContentType("text/xml");
            FileCopyUtils.copy(request.getInputStream(), response.getOutputStream());

        }
    }


}
