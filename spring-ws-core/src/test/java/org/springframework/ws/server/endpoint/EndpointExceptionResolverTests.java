/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.context.MessageContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for AbstractEndpointExceptionResolver
 *
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 */
class EndpointExceptionResolverTests {

	private MethodEndpoint methodEndpoint;

	private AbstractEndpointExceptionResolver exceptionResolver;

	@BeforeEach
	void setUp() throws Exception {

		this.exceptionResolver = new AbstractEndpointExceptionResolver() {

			@Override
			protected boolean resolveExceptionInternal(MessageContext messageContext, Object endpoint, Exception ex) {
				return true;
			}
		};

		this.exceptionResolver.setMappedEndpoints(Collections.singleton(this));
		this.methodEndpoint = new MethodEndpoint(this, getClass().getMethod("emptyMethod"));
	}

	@Test
	void testMatchMethodEndpoint() {

		boolean matched = this.exceptionResolver.resolveException(null, this.methodEndpoint, null);

		assertThat(matched).isTrue();
	}

	public void emptyMethod() {
	}

}
