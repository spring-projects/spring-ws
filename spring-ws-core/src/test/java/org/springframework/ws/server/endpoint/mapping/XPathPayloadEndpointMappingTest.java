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

package org.springframework.ws.server.endpoint.mapping;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;

public class XPathPayloadEndpointMappingTest {

	private XPathPayloadEndpointMapping mapping;

	@BeforeEach
	public void setUp() throws Exception {
		mapping = new XPathPayloadEndpointMapping();
	}

	@Test
	public void testGetLookupKeyForMessage() throws Exception {

		mapping.setExpression("/root/text()");
		mapping.afterPropertiesSet();

		MockWebServiceMessage request = new MockWebServiceMessage("<root>value</root>");
		MessageContext context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		String result = mapping.getLookupKeyForMessage(context);

		assertThat(result).isNotNull();
		assertThat(result).isEqualTo("value");
	}
}
