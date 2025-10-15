/*
 * Copyright 2005-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.http;

import java.io.File;

import javax.xml.namespace.QName;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.transport.support.EchoPayloadEndpoint;
import org.springframework.ws.transport.support.FreePortScanner;

import static org.xmlunit.assertj.XmlAssert.assertThat;

/**
 * @author Arjen Poutsma
 */
class MessageDispatcherServletIntegrationTests {

	private static Server jettyServer;

	private static String url;

	private MessageFactory messageFactory;

	private SOAPConnectionFactory connectionFactory;

	@BeforeEach
	void startJetty() throws Exception {

		int port = FreePortScanner.getFreePort();
		url = "http://localhost:" + port;

		jettyServer = new Server(port);
		Connector connector = new ServerConnector(jettyServer);
		jettyServer.addConnector(connector);

		ServletContextHandler jettyContext = new ServletContextHandler();
		jettyContext.setContextPath("/");

		String resourceBase = new File(MessageDispatcherServletIntegrationTests.class.getResource("WEB-INF").toURI())
			.getParent();

		jettyContext.setBaseResourceAsString(resourceBase);

		ServletHolder servletHolder = new ServletHolder(MessageDispatcherServlet.class);
		servletHolder.setName("sws");
		jettyContext.addServlet(servletHolder, "/");

		jettyServer.setHandler(jettyContext);
		jettyServer.start();
	}

	@BeforeEach
	void setUpSaaj() throws SOAPException {

		this.messageFactory = MessageFactory.newInstance();
		this.connectionFactory = SOAPConnectionFactory.newInstance();
	}

	@AfterEach
	void stopJetty() throws Exception {

		if (jettyServer.isRunning()) {
			jettyServer.stop();
		}
	}

	@Test
	void echo() throws SOAPException {

		SOAPMessage request = this.messageFactory.createMessage();
		SOAPElement element = request.getSOAPBody()
			.addChildElement(new QName(EchoPayloadEndpoint.NAMESPACE, EchoPayloadEndpoint.LOCAL_PART));
		element.setTextContent("Hello World");

		SOAPConnection connection = this.connectionFactory.createConnection();

		SOAPMessage response = connection.call(request, url);

		assertThat(response.getSOAPPart()).and(request.getSOAPPart()).ignoreWhitespace().areIdentical();
	}

}
