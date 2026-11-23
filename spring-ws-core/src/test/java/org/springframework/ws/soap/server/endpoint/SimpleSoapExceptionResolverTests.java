/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.server.endpoint;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.soap11.Soap11Body;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

class SimpleSoapExceptionResolverTests {

	private SimpleSoapExceptionResolver exceptionResolver;

	private MessageContext messageContext;

	private SoapMessage messageMock;

	private Soap11Body bodyMock;

	private WebServiceMessageFactory factoryMock;

	@BeforeEach
	void setUp() {

		this.exceptionResolver = new SimpleSoapExceptionResolver();
		this.factoryMock = createMock(WebServiceMessageFactory.class);
		this.messageContext = new DefaultMessageContext(new MockWebServiceMessage(), this.factoryMock);
		this.messageMock = createMock(SoapMessage.class);
		this.bodyMock = createMock(Soap11Body.class);
	}

	@Test
	void testResolveExceptionInternal() {

		Exception exception = new Exception("message");
		expect(this.factoryMock.createWebServiceMessage()).andReturn(this.messageMock);
		expect(this.messageMock.getSoapBody()).andReturn(this.bodyMock);
		expect(this.bodyMock.addServerOrReceiverFault(exception.getMessage(), Locale.ENGLISH)).andReturn(null);

		replay(this.factoryMock, this.messageMock, this.bodyMock);

		boolean result = this.exceptionResolver.resolveExceptionInternal(this.messageContext, null, exception);

		assertThat(result).isTrue();

		verify(this.factoryMock, this.messageMock, this.bodyMock);
	}

}
