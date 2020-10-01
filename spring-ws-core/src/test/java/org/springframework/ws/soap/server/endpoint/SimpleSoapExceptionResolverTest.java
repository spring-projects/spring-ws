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

package org.springframework.ws.soap.server.endpoint;

import static org.assertj.core.api.Assertions.*;
import static org.easymock.EasyMock.*;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.soap11.Soap11Body;

public class SimpleSoapExceptionResolverTest {

	private SimpleSoapExceptionResolver exceptionResolver;

	private MessageContext messageContext;

	private SoapMessage messageMock;

	private Soap11Body bodyMock;

	private WebServiceMessageFactory factoryMock;

	@BeforeEach
	public void setUp() throws Exception {

		exceptionResolver = new SimpleSoapExceptionResolver();
		factoryMock = createMock(WebServiceMessageFactory.class);
		messageContext = new DefaultMessageContext(new MockWebServiceMessage(), factoryMock);
		messageMock = createMock(SoapMessage.class);
		bodyMock = createMock(Soap11Body.class);
	}

	@Test
	public void testResolveExceptionInternal() {

		Exception exception = new Exception("message");
		expect(factoryMock.createWebServiceMessage()).andReturn(messageMock);
		expect(messageMock.getSoapBody()).andReturn(bodyMock);
		expect(bodyMock.addServerOrReceiverFault(exception.getMessage(), Locale.ENGLISH)).andReturn(null);

		replay(factoryMock, messageMock, bodyMock);

		boolean result = exceptionResolver.resolveExceptionInternal(messageContext, null, exception);

		assertThat(result).isTrue();

		verify(factoryMock, messageMock, bodyMock);
	}
}
