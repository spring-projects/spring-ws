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

package org.springframework.ws.soap.addressing.server;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.util.Iterator;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.AbstractWsAddressingTestCase;
import org.springframework.ws.soap.addressing.messageid.MessageIdStrategy;
import org.springframework.ws.soap.addressing.version.AddressingVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;

public abstract class AbstractAddressingInterceptorTestCase extends AbstractWsAddressingTestCase {

	protected AddressingEndpointInterceptor interceptor;

	protected MessageIdStrategy strategyMock;

	@Before
	public void createMocks() throws Exception {
		strategyMock = createMock(MessageIdStrategy.class);
		expect(strategyMock.isDuplicate(isA(URI.class))).andReturn(false).anyTimes();
		URI replyAction = new URI("urn:replyAction");
		URI faultAction = new URI("urn:faultAction");
		interceptor = new AddressingEndpointInterceptor(getVersion(), strategyMock, new WebServiceMessageSender[0],
				replyAction, faultAction);
	}

	@Test
	public void testUnderstands() throws Exception {
		SaajSoapMessage validRequest = loadSaajMessage(getTestPath() + "/valid.xml");
		Iterator<SoapHeaderElement> iterator = validRequest.getSoapHeader().examineAllHeaderElements();

		replay(strategyMock);

		while (iterator.hasNext()) {
			SoapHeaderElement headerElement = iterator.next();
			assertTrue("Header [" + headerElement.getName() + " not understood", interceptor.understands(headerElement));
		}

		verify(strategyMock);
	}

	@Test
	public void testValidRequest() throws Exception {
		SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/valid.xml");
		MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(messageFactory));

		replay(strategyMock);

		boolean result = interceptor.handleRequest(context, null);
		assertTrue("Valid request not handled", result);
		assertFalse("Message Context has response", context.hasResponse());

		verify(strategyMock);
	}

	@Test
	public void testNoMessageId() throws Exception {
		SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/request-no-message-id.xml");
		MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(messageFactory));

		replay(strategyMock);

		boolean result = interceptor.handleRequest(context, null);
		assertFalse("Request with no MessageID handled", result);
		assertTrue("Message Context has no response", context.hasResponse());
		SaajSoapMessage expectedResponse = loadSaajMessage(getTestPath() + "/response-no-message-id.xml");
		assertXMLEqual("Invalid response for message with no MessageID", expectedResponse,
				(SaajSoapMessage) context.getResponse());

		verify(strategyMock);
	}

	@Test
	public void testNoReplyTo() throws Exception {
		SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/request-no-reply-to.xml");
		MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(messageFactory));
		URI messageId = new URI("uid:1234");

		expect(strategyMock.newMessageId((SoapMessage) context.getResponse())).andReturn(messageId);
		replay(strategyMock);

		boolean result = interceptor.handleResponse(context, null);
		assertTrue("Request with no ReplyTo not handled", result);
		assertTrue("Message Context has no response", context.hasResponse());
		SaajSoapMessage expectedResponse = loadSaajMessage(getTestPath() + "/response-anonymous.xml");
		assertXMLEqual("Invalid response for message with invalid MAP", expectedResponse,
				(SaajSoapMessage) context.getResponse());

		verify(strategyMock);
	}

	@Test
	public void testAnonymousReplyTo() throws Exception {
		SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/request-anonymous.xml");
		MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(messageFactory));
		URI messageId = new URI("uid:1234");

		expect(strategyMock.newMessageId((SoapMessage) context.getResponse())).andReturn(messageId);
		replay(strategyMock);

		boolean result = interceptor.handleResponse(context, null);
		assertTrue("Request with anonymous ReplyTo not handled", result);
		SaajSoapMessage expectedResponse = loadSaajMessage(getTestPath() + "/response-anonymous.xml");
		assertXMLEqual("Invalid response for message with invalid MAP", expectedResponse,
				(SaajSoapMessage) context.getResponse());

		verify(strategyMock);
	}

	@Test
	public void testNoneReplyTo() throws Exception {
		SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/request-none.xml");
		MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(messageFactory));
		replay(strategyMock);
		boolean result = interceptor.handleResponse(context, null);
		assertFalse("None request handled", result);
		assertFalse("Message context has response", context.hasResponse());
		verify(strategyMock);
	}

	@Test
	public void testFaultTo() throws Exception {
		SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/request-fault-to.xml");
		MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(messageFactory));
		SaajSoapMessage response = (SaajSoapMessage) context.getResponse();
		response.getSoapBody().addServerOrReceiverFault("Error", Locale.ENGLISH);
		URI messageId = new URI("uid:1234");
		expect(strategyMock.newMessageId((SoapMessage) context.getResponse())).andReturn(messageId);
		replay(strategyMock);
		boolean result = interceptor.handleFault(context, null);
		assertTrue("Request with anonymous FaultTo not handled", result);
		SaajSoapMessage expectedResponse = loadSaajMessage(getTestPath() + "/response-fault-to.xml");
		assertXMLEqual("Invalid response for message with invalid MAP", expectedResponse,
				(SaajSoapMessage) context.getResponse());
		verify(strategyMock);
	}

	@Test
	public void testOutOfBandReplyTo() throws Exception {
		WebServiceMessageSender senderMock = createMock(WebServiceMessageSender.class);

		URI replyAction = new URI("urn:replyAction");
		URI faultAction = new URI("urn:replyAction");
		interceptor = new AddressingEndpointInterceptor(getVersion(), strategyMock,
				new WebServiceMessageSender[] { senderMock }, replyAction, faultAction);

		WebServiceConnection connectionMock = createMock(WebServiceConnection.class);

		SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/valid.xml");
		MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(messageFactory));
		SaajSoapMessage response = (SaajSoapMessage) context.getResponse();

		URI messageId = new URI("uid:1234");
		expect(strategyMock.newMessageId((SoapMessage) context.getResponse())).andReturn(messageId);

		URI uri = new URI("http://example.com/business/client1");
		expect(senderMock.supports(uri)).andReturn(true);
		expect(senderMock.createConnection(uri)).andReturn(connectionMock);
		connectionMock.send(response);
		connectionMock.close();

		replay(strategyMock, senderMock, connectionMock);

		boolean result = interceptor.handleResponse(context, null);
		assertFalse("Out of Band request handled", result);
		assertFalse("Message context has response", context.hasResponse());

		verify(strategyMock, senderMock, connectionMock);
	}

	protected abstract AddressingVersion getVersion();

	protected abstract String getTestPath();

}
