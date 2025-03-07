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

package org.springframework.ws.soap.server.endpoint.mapping;

import jakarta.xml.soap.MessageFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class SoapActionEndpointMappingTest {

	private SoapActionEndpointMapping mapping;

	private MessageContext context;

	@BeforeEach
	public void setUp() throws Exception {

		this.mapping = new SoapActionEndpointMapping();
		this.context = new DefaultMessageContext(new SaajSoapMessageFactory(MessageFactory.newInstance()));
	}

	@Test
	public void testGetLookupKeyForMessage() throws Exception {

		String soapAction = "http://springframework.org/spring-ws/SoapAction";
		((SoapMessage) this.context.getRequest()).setSoapAction(soapAction);

		assertThat(this.mapping.getLookupKeyForMessage(this.context)).isEqualTo(soapAction);
	}

	@Test
	public void testGetLookupKeyForMessageQuoted() throws Exception {

		String soapAction = "http://springframework.org/spring-ws/SoapAction";
		((SoapMessage) this.context.getRequest()).setSoapAction(soapAction);

		assertThat(this.mapping.getLookupKeyForMessage(this.context)).isEqualTo(soapAction);
	}

	@Test
	public void testValidateLookupKey() {
		assertThat(this.mapping.validateLookupKey("SoapAction")).isTrue();
	}

}
