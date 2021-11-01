/*
 * Copyright 2005-2018 the original author or authors.
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

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.soap.MessageFactory;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.URIException;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.support.FreePortScanner;

public class CommonsHttpMessageSenderIntegrationTest
		extends AbstractHttpWebServiceMessageSenderIntegrationTestCase<CommonsHttpMessageSender> {

	@Override
	protected CommonsHttpMessageSender createMessageSender() {
		return new CommonsHttpMessageSender();
	}

	@Test
	public void testMaxConnections() throws URIException {

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
		Connector connector = new ServerConnector(jettyServer);
		jettyServer.addConnector(connector);

		ServletContextHandler jettyContext = new ServletContextHandler();
		jettyContext.setContextPath("/");

		jettyContext.addServlet(EchoServlet.class, "/");

		jettyServer.setHandler(jettyContext);
		jettyServer.start();

		WebServiceConnection connection = null;

		try {

			StaticApplicationContext appContext = new StaticApplicationContext();
			appContext.registerSingleton("messageSender", CommonsHttpMessageSender.class);
			appContext.refresh();

			CommonsHttpMessageSender messageSender = appContext.getBean("messageSender", CommonsHttpMessageSender.class);
			connection = messageSender.createConnection(new URI("http://localhost:" + port));

			appContext.close();

			connection.send(new SaajSoapMessage(messageFactory.createMessage()));
			connection.receive(new SaajSoapMessageFactory(messageFactory));
		} finally {

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

	@SuppressWarnings("serial")
	public static class EchoServlet extends HttpServlet {

		@Override
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

			response.setContentType("text/xml");
			FileCopyUtils.copy(request.getInputStream(), response.getOutputStream());
		}
	}
}
