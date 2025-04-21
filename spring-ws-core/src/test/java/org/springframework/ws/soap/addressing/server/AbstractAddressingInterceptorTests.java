/*
 * Copyright 2005-2025 the original author or authors.
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

package org.springframework.ws.soap.addressing.server;

import java.net.URI;
import java.util.Iterator;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.AbstractWsAddressingTests;
import org.springframework.ws.soap.addressing.messageid.MessageIdStrategy;
import org.springframework.ws.soap.addressing.version.AddressingVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public abstract class AbstractAddressingInterceptorTests extends AbstractWsAddressingTests {

	protected AddressingEndpointInterceptor interceptor;

	protected MessageIdStrategy strategyMock;

	@BeforeEach
	void createMocks() throws Exception {

		this.strategyMock = createMock(MessageIdStrategy.class);
		expect(this.strategyMock.isDuplicate(isA(URI.class))).andReturn(false).anyTimes();
		URI replyAction = new URI("urn:replyAction");
		URI faultAction = new URI("urn:faultAction");
		this.interceptor = new AddressingEndpointInterceptor(getVersion(), this.strategyMock,
				new WebServiceMessageSender[0], replyAction, faultAction);
	}

	@Test
	void testUnderstands() throws Exception {

		SaajSoapMessage validRequest = loadSaajMessage(getTestPath() + "/valid.xml");
		Iterator<SoapHeaderElement> iterator = validRequest.getSoapHeader().examineAllHeaderElements();

		replay(this.strategyMock);

		while (iterator.hasNext()) {
			SoapHeaderElement headerElement = iterator.next();
			assertThat(this.interceptor.understands(headerElement)).isTrue();
		}

		verify(this.strategyMock);
	}

	@Test
	void testValidRequest() throws Exception {

		SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/valid.xml");
		MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(this.messageFactory));

		replay(this.strategyMock);

		boolean result = this.interceptor.handleRequest(context, null);

		assertThat(result).isTrue();
		assertThat(context.hasResponse()).isFalse();

		verify(this.strategyMock);
	}

	@Test
	void testNoMessageId() throws Exception {

		SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/request-no-message-id.xml");
		MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(this.messageFactory));

		replay(this.strategyMock);

		boolean result = this.interceptor.handleRequest(context, null);

		assertThat(result).isFalse();
		assertThat(context.hasResponse()).isTrue();

		SaajSoapMessage expectedResponse = loadSaajMessage(getTestPath() + "/response-no-message-id.xml");

		assertXMLSimilar(expectedResponse, (SaajSoapMessage) context.getResponse());

		verify(this.strategyMock);
	}

	@Test
	void testNoReplyTo() throws Exception {

		SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/request-no-reply-to.xml");
		MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(this.messageFactory));
		URI messageId = new URI("uid:1234");

		expect(this.strategyMock.newMessageId((SoapMessage) context.getResponse())).andReturn(messageId);
		replay(this.strategyMock);

		boolean result = this.interceptor.handleResponse(context, null);

		assertThat(result).isTrue();
		assertThat(context.hasResponse()).isTrue();

		SaajSoapMessage expectedResponse = loadSaajMessage(getTestPath() + "/response-anonymous.xml");

		assertXMLNotSimilar(expectedResponse, (SaajSoapMessage) context.getResponse());

		verify(this.strategyMock);
	}

	@Test
	void testAnonymousReplyTo() throws Exception {

		SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/request-anonymous.xml");
		MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(this.messageFactory));
		URI messageId = new URI("uid:1234");

		expect(this.strategyMock.newMessageId((SoapMessage) context.getResponse())).andReturn(messageId);
		replay(this.strategyMock);

		boolean result = this.interceptor.handleResponse(context, null);

		assertThat(result).isTrue();

		SaajSoapMessage expectedResponse = loadSaajMessage(getTestPath() + "/response-anonymous.xml");

		assertXMLNotSimilar(expectedResponse, (SaajSoapMessage) context.getResponse());

		verify(this.strategyMock);
	}

	@Test
	protected void testNoneReplyTo() throws Exception {

		SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/request-none.xml");
		MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(this.messageFactory));
		replay(this.strategyMock);
		boolean result = this.interceptor.handleResponse(context, null);

		assertThat(result).isFalse();
		assertThat(context.hasResponse()).isFalse();

		verify(this.strategyMock);
	}

	@Test
	void testFaultTo() throws Exception {

		SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/request-fault-to.xml");
		MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(this.messageFactory));
		SaajSoapMessage response = (SaajSoapMessage) context.getResponse();
		response.getSoapBody().addServerOrReceiverFault("Error", Locale.ENGLISH);
		URI messageId = new URI("uid:1234");

		expect(this.strategyMock.newMessageId((SoapMessage) context.getResponse())).andReturn(messageId);

		replay(this.strategyMock);

		boolean result = this.interceptor.handleFault(context, null);

		assertThat(result).isTrue();

		SaajSoapMessage expectedResponse = loadSaajMessage(getTestPath() + "/response-fault-to.xml");

		assertXMLNotSimilar(expectedResponse, (SaajSoapMessage) context.getResponse());

		verify(this.strategyMock);
	}

	@Test
	void testOutOfBandReplyTo() throws Exception {

		WebServiceMessageSender senderMock = createMock(WebServiceMessageSender.class);

		URI replyAction = new URI("urn:replyAction");
		URI faultAction = new URI("urn:replyAction");
		this.interceptor = new AddressingEndpointInterceptor(getVersion(), this.strategyMock,
				new WebServiceMessageSender[] { senderMock }, replyAction, faultAction);

		WebServiceConnection connectionMock = createMock(WebServiceConnection.class);

		SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/valid.xml");
		MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(this.messageFactory));
		SaajSoapMessage response = (SaajSoapMessage) context.getResponse();

		URI messageId = new URI("uid:1234");
		expect(this.strategyMock.newMessageId((SoapMessage) context.getResponse())).andReturn(messageId);

		URI uri = new URI("http://example.com/business/client1");
		expect(senderMock.supports(uri)).andReturn(true);
		expect(senderMock.createConnection(uri)).andReturn(connectionMock);
		connectionMock.send(response);
		connectionMock.close();

		replay(this.strategyMock, senderMock, connectionMock);

		boolean result = this.interceptor.handleResponse(context, null);

		assertThat(result).isFalse();
		assertThat(context.hasResponse()).isFalse();

		verify(this.strategyMock, senderMock, connectionMock);
	}

	protected abstract AddressingVersion getVersion();

	protected abstract String getTestPath();

}
