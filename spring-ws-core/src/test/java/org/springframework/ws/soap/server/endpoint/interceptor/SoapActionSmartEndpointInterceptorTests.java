/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.server.endpoint.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class SoapActionSmartEndpointInterceptorTests {

	private EndpointInterceptor delegate;

	private String soapAction;

	private MessageContext messageContext;

	@BeforeEach
	void setUp() {

		this.delegate = new EndpointInterceptorAdapter();

		this.soapAction = "http://springframework.org/spring-ws";

		SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory();
		messageFactory.afterPropertiesSet();
		SaajSoapMessage request = messageFactory.createWebServiceMessage();
		request.setSoapAction(this.soapAction);
		this.messageContext = new DefaultMessageContext(request, messageFactory);
	}

	@Test
	void neitherNamespaceNorLocalPart() {
		assertThatIllegalArgumentException()
			.isThrownBy(() -> new SoapActionSmartEndpointInterceptor(this.delegate, null));
	}

	@Test
	void shouldInterceptMatch() {

		SoapActionSmartEndpointInterceptor interceptor = new SoapActionSmartEndpointInterceptor(this.delegate,
				this.soapAction);

		boolean result = interceptor.shouldIntercept(this.messageContext, null);

		assertThat(result).isTrue();
	}

	@Test
	void shouldInterceptNonMatch() {

		SoapActionSmartEndpointInterceptor interceptor = new SoapActionSmartEndpointInterceptor(this.delegate,
				"http://springframework.org/other");

		boolean result = interceptor.shouldIntercept(this.messageContext, null);

		assertThat(result).isFalse();
	}

}
