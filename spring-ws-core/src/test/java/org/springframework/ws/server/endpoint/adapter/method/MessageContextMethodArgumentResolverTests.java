/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.adapter.method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.MethodParameter;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;

import static org.assertj.core.api.Assertions.assertThat;

class MessageContextMethodArgumentResolverTests {

	private MessageContextMethodArgumentResolver resolver;

	private MethodParameter supported;

	@BeforeEach
	void setUp() throws NoSuchMethodException {

		this.resolver = new MessageContextMethodArgumentResolver();
		this.supported = new MethodParameter(getClass().getMethod("supported", MessageContext.class), 0);
	}

	@Test
	void supportsParameter() {
		assertThat(this.resolver.supportsParameter(this.supported)).isTrue();
	}

	@Test
	void resolveArgument() throws Exception {

		MessageContext messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());

		MessageContext result = this.resolver.resolveArgument(messageContext, this.supported);

		assertThat(result).isSameAs(messageContext);
	}

	public void supported(MessageContext messageContext) {
	}

}
