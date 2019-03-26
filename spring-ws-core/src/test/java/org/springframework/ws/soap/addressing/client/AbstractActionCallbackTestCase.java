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

package org.springframework.ws.soap.addressing.client;

import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.ws.soap.addressing.AbstractWsAddressingTestCase;
import org.springframework.ws.soap.addressing.core.EndpointReference;
import org.springframework.ws.soap.addressing.messageid.MessageIdStrategy;
import org.springframework.ws.soap.addressing.version.AddressingVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.context.DefaultTransportContext;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;

import static org.easymock.EasyMock.*;

public abstract class AbstractActionCallbackTestCase extends AbstractWsAddressingTestCase {

	private ActionCallback callback;

	private MessageIdStrategy strategyMock;

	private WebServiceConnection connectionMock;

	@Before
	public void createMocks() throws Exception {
		strategyMock = createMock(MessageIdStrategy.class);

		connectionMock = createMock(WebServiceConnection.class);

		TransportContext transportContext = new DefaultTransportContext(connectionMock);
		TransportContextHolder.setTransportContext(transportContext);
	}

	@After
	public void clearContext() throws Exception {
		TransportContextHolder.setTransportContext(null);
	}

	@Test
	public void testValid() throws Exception {
		URI action = new URI("http://example.com/fabrikam/mail/Delete");
		URI to = new URI("mailto:fabrikam@example.com");
		callback = new ActionCallback(action, getVersion(), to);
		callback.setMessageIdStrategy(strategyMock);
		SaajSoapMessage message = createDeleteMessage();
		expect(strategyMock.newMessageId(message)).andReturn(new URI("https://example.com/someuniquestring"));
		callback.setReplyTo(new EndpointReference(new URI("https://example.com/business/client1")));

		replay(strategyMock, connectionMock);

		callback.doWithMessage(message);

		SaajSoapMessage expected = loadSaajMessage(getTestPath() + "/valid.xml");
		assertXMLSimilar("Invalid message", expected, message);

		verify(strategyMock, connectionMock);
	}

	@Test
	public void testDefaults() throws Exception {
		URI action = new URI("http://example.com/fabrikam/mail/Delete");
		URI connectionUri = new URI("mailto:fabrikam@example.com");
		callback = new ActionCallback(action, getVersion());
		callback.setMessageIdStrategy(strategyMock);
		expect(connectionMock.getUri()).andReturn(connectionUri);

		SaajSoapMessage message = createDeleteMessage();
		expect(strategyMock.newMessageId(message)).andReturn(new URI("https://example.com/someuniquestring"));
		callback.setReplyTo(new EndpointReference(new URI("https://example.com/business/client1")));

		replay(strategyMock, connectionMock);

		callback.doWithMessage(message);

		SaajSoapMessage expected = loadSaajMessage(getTestPath() + "/valid.xml");
		assertXMLSimilar("Invalid message", expected, message);
		verify(strategyMock, connectionMock);
	}

	private SaajSoapMessage createDeleteMessage() throws SOAPException {
		SOAPMessage saajMessage = messageFactory.createMessage();
		SOAPBody saajBody = saajMessage.getSOAPBody();
		SOAPBodyElement delete = saajBody.addBodyElement(new QName("http://example.com/fabrikam", "Delete"));
		SOAPElement maxCount = delete.addChildElement(new QName("maxCount"));
		maxCount.setTextContent("42");
		return new SaajSoapMessage(saajMessage);
	}

	protected abstract AddressingVersion getVersion();

	protected abstract String getTestPath();

}