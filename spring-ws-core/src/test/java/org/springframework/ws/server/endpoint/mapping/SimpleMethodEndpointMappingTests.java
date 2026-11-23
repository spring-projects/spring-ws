/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.mapping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleMethodEndpointMappingTests {

	private SimpleMethodEndpointMapping mapping;

	@BeforeEach
	void setUp() throws Exception {

		this.mapping = new SimpleMethodEndpointMapping();
		this.mapping.setMethodPrefix("prefix");
		this.mapping.setMethodSuffix("Suffix");

		MyBean bean = new MyBean();

		this.mapping.setEndpoints(new Object[] { bean });
		this.mapping.afterPropertiesSet();
	}

	@Test
	void testRegistration() {

		assertThat(this.mapping.lookupEndpoint("MyRequest")).isNotNull();
		assertThat(this.mapping.lookupEndpoint("request")).isNull();
	}

	@Test
	void testGetLookupKeyForMessageNoNamespace() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage("<MyRequest/>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		assertThat(this.mapping.getLookupKeyForMessage(messageContext)).isEqualTo("MyRequest");
	}

	@Test
	void testGetLookupKeyForMessageNamespace() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage(
				"<MyRequest xmlns='http://springframework.org/spring-ws/' />");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		assertThat(this.mapping.getLookupKeyForMessage(messageContext)).isEqualTo("MyRequest");
	}

	private static final class MyBean {

		public void prefixMyRequestSuffix() {

		}

		public void request() {

		}

	}

}
