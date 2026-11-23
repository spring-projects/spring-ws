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

class XPathPayloadEndpointMappingTests {

	private XPathPayloadEndpointMapping mapping;

	@BeforeEach
	void setUp() {
		this.mapping = new XPathPayloadEndpointMapping();
	}

	@Test
	void testGetLookupKeyForMessage() throws Exception {

		this.mapping.setExpression("/root/text()");
		this.mapping.afterPropertiesSet();

		MockWebServiceMessage request = new MockWebServiceMessage("<root>value</root>");
		MessageContext context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		String result = this.mapping.getLookupKeyForMessage(context);

		assertThat(result).isNotNull();
		assertThat(result).isEqualTo("value");
	}

}
