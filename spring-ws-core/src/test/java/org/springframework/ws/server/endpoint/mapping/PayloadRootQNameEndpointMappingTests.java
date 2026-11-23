/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.mapping;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;

import static org.assertj.core.api.Assertions.assertThat;

class PayloadRootQNameEndpointMappingTests {

	private PayloadRootQNameEndpointMapping mapping;

	@BeforeEach
	void setUp() {
		this.mapping = new PayloadRootQNameEndpointMapping();
	}

	@Test
	void testResolveQNames() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage("<root/>");
		MessageContext context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		QName qName = this.mapping.resolveQName(context);

		assertThat(qName).isNotNull();
		assertThat(qName).isEqualTo(new QName("root"));
	}

	@Test
	void testGetQNameNameNamespace() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage("<prefix:localname xmlns:prefix=\"namespace\"/>");
		MessageContext context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		QName qName = this.mapping.resolveQName(context);

		assertThat(qName).isNotNull();
		assertThat(qName).isEqualTo(new QName("namespace", "localname", "prefix"));
	}

}
