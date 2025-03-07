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

public class SimpleSoapExceptionResolverTest {

	private SimpleSoapExceptionResolver exceptionResolver;

	private MessageContext messageContext;

	private SoapMessage messageMock;

	private Soap11Body bodyMock;

	private WebServiceMessageFactory factoryMock;

	@BeforeEach
	public void setUp() {

		this.exceptionResolver = new SimpleSoapExceptionResolver();
		this.factoryMock = createMock(WebServiceMessageFactory.class);
		this.messageContext = new DefaultMessageContext(new MockWebServiceMessage(), this.factoryMock);
		this.messageMock = createMock(SoapMessage.class);
		this.bodyMock = createMock(Soap11Body.class);
	}

	@Test
	public void testResolveExceptionInternal() {

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
