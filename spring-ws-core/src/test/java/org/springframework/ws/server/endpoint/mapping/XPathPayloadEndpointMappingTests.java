/*
 * Copyright 2005-present the original author or authors.
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
