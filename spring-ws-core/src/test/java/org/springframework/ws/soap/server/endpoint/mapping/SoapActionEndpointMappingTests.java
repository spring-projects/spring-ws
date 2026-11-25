/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SoapActionEndpointMappingTests {

	private SoapActionEndpointMapping mapping;

	private MessageContext context;

	@BeforeEach
	void setUp() throws Exception {

		this.mapping = new SoapActionEndpointMapping();
		this.context = new DefaultMessageContext(new SaajSoapMessageFactory(MessageFactory.newInstance()));
	}

	@Test
	void testGetLookupKeyForMessage() throws Exception {

		String soapAction = "http://springframework.org/spring-ws/SoapAction";
		((SoapMessage) this.context.getRequest()).setSoapAction(soapAction);

		assertThat(this.mapping.getLookupKeyForMessage(this.context)).isEqualTo(soapAction);
	}

	@Test
	void testGetLookupKeyForMessageQuoted() throws Exception {

		String soapAction = "http://springframework.org/spring-ws/SoapAction";
		((SoapMessage) this.context.getRequest()).setSoapAction(soapAction);

		assertThat(this.mapping.getLookupKeyForMessage(this.context)).isEqualTo(soapAction);
	}

	@Test
	void getLookupKeyForMessageWithSingleQuote() throws Exception {
		SoapMessage request = mock(SoapMessage.class);
		when(request.getSoapAction()).thenReturn("\"");
		MessageContext messageContext = new DefaultMessageContext(request, mock());

		assertThat(this.mapping.getLookupKeyForMessage(messageContext)).isEqualTo("\"");
	}

	@Test
	void testValidateLookupKey() {
		assertThat(this.mapping.validateLookupKey("SoapAction")).isTrue();
	}

}
