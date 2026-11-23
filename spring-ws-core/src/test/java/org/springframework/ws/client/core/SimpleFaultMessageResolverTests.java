/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.client.WebServiceFaultException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

class SimpleFaultMessageResolverTests {

	private SimpleFaultMessageResolver resolver;

	@BeforeEach
	void setUp() {
		this.resolver = new SimpleFaultMessageResolver();
	}

	@Test
	void testResolveFault() {

		FaultAwareWebServiceMessage messageMock = createMock(FaultAwareWebServiceMessage.class);
		String message = "message";
		expect(messageMock.getFaultReason()).andReturn(message);

		replay(messageMock);

		assertThatExceptionOfType(WebServiceFaultException.class)
			.isThrownBy(() -> this.resolver.resolveFault(messageMock))
			.withMessage(message);

		verify(messageMock);
	}

}
