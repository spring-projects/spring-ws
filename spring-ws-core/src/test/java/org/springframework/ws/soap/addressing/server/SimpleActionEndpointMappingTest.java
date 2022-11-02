/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ws.soap.addressing.server;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.endpoint.interceptor.PayloadLoggingInterceptor;
import org.springframework.ws.soap.addressing.AbstractWsAddressingTestCase;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadValidatingInterceptor;

public class SimpleActionEndpointMappingTest extends AbstractWsAddressingTestCase {

	private SimpleActionEndpointMapping mapping;

	private Endpoint1 endpoint1;

	@BeforeEach
	public void createMappings() throws Exception {

		mapping = new SimpleActionEndpointMapping();

		endpoint1 = new Endpoint1();
		Endpoint2 endpoint2 = new Endpoint2();

		Map<String, Object> map = new HashMap<>();
		map.put("http://example.com/fabrikam/mail/Delete", endpoint1);
		map.put("http://example.com/fabrikam/mail/Add", endpoint2);

		mapping.setPreInterceptors(new EndpointInterceptor[] { new PayloadLoggingInterceptor() });
		mapping.setPostInterceptors(new EndpointInterceptor[] { new PayloadValidatingInterceptor() });
		mapping.setAddress(new URI("mailto:fabrikam@example.com"));
		mapping.setActionMap(map);
		mapping.afterPropertiesSet();
	}

	@Test
	public void testMatch() throws Exception {

		SaajSoapMessage message = loadSaajMessage("200408/valid.xml");
		MessageContext messageContext = new DefaultMessageContext(message, new SaajSoapMessageFactory(messageFactory));

		EndpointInvocationChain endpoint = mapping.getEndpoint(messageContext);

		assertThat(endpoint).isNotNull();
		assertThat(endpoint.getEndpoint()).isEqualTo(endpoint1);

		EndpointInterceptor[] interceptors = endpoint.getInterceptors();

		assertThat(interceptors).hasSize(3);
		assertThat(interceptors[0]).isInstanceOf(PayloadLoggingInterceptor.class);
		assertThat(interceptors[1]).isInstanceOf(AddressingEndpointInterceptor.class);
		assertThat(interceptors[2]).isInstanceOf(PayloadValidatingInterceptor.class);
	}

	@Test
	public void testNoMatch() throws Exception {

		SaajSoapMessage message = loadSaajMessage("200408/response-no-message-id.xml");
		MessageContext messageContext = new DefaultMessageContext(message, new SaajSoapMessageFactory(messageFactory));

		EndpointInvocationChain endpoint = mapping.getEndpoint(messageContext);

		assertThat(endpoint).isNull();
	}

	private static class Endpoint1 {

	}

	private static class Endpoint2 {

	}
}
