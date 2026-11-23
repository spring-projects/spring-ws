/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.addressing.server;

import java.net.URI;

import org.junit.jupiter.api.Test;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.version.Addressing10;
import org.springframework.ws.soap.addressing.version.AddressingVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

class AddressingInterceptor10Tests extends AbstractAddressingInterceptorTests {

	@Override
	protected AddressingVersion getVersion() {
		return new Addressing10();
	}

	@Override
	protected String getTestPath() {
		return "10";
	}

	@Test
	void testNoTo() throws Exception {
		SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/request-no-to.xml");
		MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(this.messageFactory));
		URI messageId = new URI("uid:1234");
		expect(this.strategyMock.newMessageId((SoapMessage) context.getResponse())).andReturn(messageId);
		replay(this.strategyMock);

		boolean result = this.interceptor.handleResponse(context, null);
		assertThat(result).isTrue();
		assertThat(context.hasResponse()).isTrue();
		SaajSoapMessage expectedResponse = loadSaajMessage(getTestPath() + "/response-anonymous.xml");
		assertXMLSimilar(expectedResponse, (SaajSoapMessage) context.getResponse());
		verify(this.strategyMock);
	}

	@Test
	void testAnonymousReplyToWithoutMessageId() throws Exception {
		SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/request-anonymous-no-message-id.xml");
		MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(this.messageFactory));
		expect(this.strategyMock.isDuplicate(null)).andReturn(false);
		replay(this.strategyMock);

		boolean result = this.interceptor.handleRequest(context, null);
		assertThat(result).isTrue();
		assertThat(context.hasResponse()).isFalse();
		verify(this.strategyMock);
	}

	@Test
	void testNoReplyToWithoutMessageId() throws Exception {
		SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/request-no-reply-to-no-message-id.xml");
		MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(this.messageFactory));
		expect(this.strategyMock.isDuplicate(null)).andReturn(false);
		replay(this.strategyMock);

		boolean result = this.interceptor.handleRequest(context, null);
		assertThat(result).isTrue();
		assertThat(context.hasResponse()).isFalse();
		verify(this.strategyMock);
	}

	@Test
	void testNoneReplyToWithoutMessageId() throws Exception {
		SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/request-none-no-message-id.xml");
		MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(this.messageFactory));
		expect(this.strategyMock.isDuplicate(null)).andReturn(false);
		replay(this.strategyMock);

		boolean result = this.interceptor.handleRequest(context, null);
		assertThat(result).isTrue();
		assertThat(context.hasResponse()).isFalse();
		verify(this.strategyMock);
	}

	@Test
	void testAnonymousReplyToWithNonAnonymousFaultToWithoutMessageId() throws Exception {
		SaajSoapMessage valid = loadSaajMessage(
				getTestPath() + "/request-anonymous-reply-to-fault-to-no-message-id.xml");
		MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(this.messageFactory));
		replay(this.strategyMock);

		boolean result = this.interceptor.handleRequest(context, null);
		assertThat(result).isFalse();
		assertThat(context.hasResponse()).isTrue();
		SaajSoapMessage expectedResponse = loadSaajMessage(
				getTestPath() + "/response-anonymous-reply-to-fault-to-no-message-id.xml");
		assertXMLSimilar(expectedResponse, (SaajSoapMessage) context.getResponse());
		verify(this.strategyMock);
	}

}
