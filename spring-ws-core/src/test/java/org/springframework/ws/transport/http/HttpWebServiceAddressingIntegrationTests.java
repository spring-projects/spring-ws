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
import java.util.UUID;

import javax.xml.namespace.QName;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.Name;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPHeaderElement;
import jakarta.xml.soap.SOAPMessage;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.transport.support.FreePortScanner;
import org.springframework.ws.transport.support.WsAddressingEndpoint;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for WS Addressing and HTTP.
 *
 * @author Stephane Nicoll
 */
class HttpWebServiceAddressingIntegrationTest {

	private static final String WSA_NS = "http://schemas.xmlsoap.org/ws/2004/08/addressing";

	private static final String WSA_ANONYMOUS = "http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous";

	private static final String LINK_LOCAL_URI = "http://169.254.169.254/latest/api/token";

	private static Server jettyServer;

	private static String url;

	private MessageFactory messageFactory;

	private SOAPConnectionFactory connectionFactory;

	@BeforeAll
	static void startJetty() throws Exception {
		int port = FreePortScanner.getFreePort();
		url = "http://localhost:" + port;
		jettyServer = new Server(port);
		Connector connector = new ServerConnector(jettyServer);
		jettyServer.addConnector(connector);
		ServletContextHandler jettyContext = new ServletContextHandler();
		jettyContext.setContextPath("/");
		String resourceBase = new File(HttpWebServiceAddressingIntegrationTest.class.getResource("WEB-INF").toURI())
			.getParent();
		jettyContext.setBaseResourceAsString(resourceBase);
		ServletHolder servletHolder = new ServletHolder(new MessageDispatcherServlet());
		servletHolder.setName("wsa");
		jettyContext.addServlet(servletHolder, "/");
		jettyServer.setHandler(jettyContext);
		jettyServer.start();
	}

	@AfterAll
	static void stopJetty() throws Exception {
		if (jettyServer.isRunning()) {
			jettyServer.stop();
		}
	}

	@BeforeEach
	void setUp() throws SOAPException {
		this.messageFactory = MessageFactory.newInstance();
		this.connectionFactory = SOAPConnectionFactory.newInstance();
	}

	@Test
	void replyToLinkLocalAddressIsRejectedWithAddressingFault() throws Exception {
		SOAPMessage request = createRequest(WsAddressingEndpoint.REPLY_ACTION, LINK_LOCAL_URI, WSA_ANONYMOUS);
		SOAPConnection connection = this.connectionFactory.createConnection();

		SOAPMessage response = connection.call(request, url);
		assertInvalidAddressingHeaderFault(response);
	}

	@Test
	void faultToLinkLocalAddressIsRejectedWithAddressingFault() throws Exception {
		SOAPMessage request = createRequest(WsAddressingEndpoint.FAULT_ACTION, WSA_ANONYMOUS, LINK_LOCAL_URI);
		SOAPConnection connection = this.connectionFactory.createConnection();

		SOAPMessage response = connection.call(request, url);
		assertInvalidAddressingHeaderFault(response);
	}

	private void assertInvalidAddressingHeaderFault(SOAPMessage response) throws SOAPException {
		SOAPFault fault = response.getSOAPBody().getFault();
		assertThat(fault).as("expected a SOAP fault").isNotNull();
		QName faultCode = fault.getFaultCodeAsQName();
		assertThat(faultCode.getNamespaceURI()).isEqualTo(WSA_NS);
		assertThat(faultCode.getLocalPart()).isEqualTo("InvalidMessageInformationHeader");
		assertThat(fault.getFaultString())
			.isEqualTo("A message information header is not valid and the message cannot be processed.");
	}

	private SOAPMessage createRequest(String action, String replyTo, String faultTo) throws SOAPException {
		SOAPMessage message = this.messageFactory.createMessage();
		SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
		SOAPHeader header = envelope.getHeader();
		addTextHeader(header, envelope, "MessageID", "urn:uuid:" + UUID.randomUUID());
		addTextHeader(header, envelope, "Action", action);
		addTextHeader(header, envelope, "To", url);
		addAddressHeader(header, envelope, "ReplyTo", replyTo);
		addAddressHeader(header, envelope, "FaultTo", faultTo);
		message.getSOAPBody().addChildElement("Delete", "f", "http://example.com/fabrikam");
		return message;
	}

	private void addTextHeader(SOAPHeader header, SOAPEnvelope envelope, String localName, String value)
			throws SOAPException {
		Name name = envelope.createName(localName, "wsa", WSA_NS);
		SOAPHeaderElement element = header.addHeaderElement(name);
		element.addTextNode(value);
	}

	private void addAddressHeader(SOAPHeader header, SOAPEnvelope envelope, String localName, String address)
			throws SOAPException {
		Name name = envelope.createName(localName, "wsa", WSA_NS);
		SOAPHeaderElement element = header.addHeaderElement(name);
		SOAPElement addressElement = element.addChildElement(envelope.createName("Address", "wsa", WSA_NS));
		addressElement.addTextNode(address);
	}

}
