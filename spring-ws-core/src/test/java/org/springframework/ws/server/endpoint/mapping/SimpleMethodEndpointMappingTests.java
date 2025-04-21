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
