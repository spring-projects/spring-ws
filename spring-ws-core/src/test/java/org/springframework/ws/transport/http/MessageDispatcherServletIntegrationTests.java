/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
