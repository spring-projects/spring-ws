/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.server.endpoint.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class PayloadRootSmartSoapEndpointInterceptorTests {

	private EndpointInterceptor delegate;

	private String namespaceUri;

	private String localPart;

	private MessageContext messageContext;

	@BeforeEach
	void setUp() {

		this.delegate = new EndpointInterceptorAdapter();

		this.namespaceUri = "http://springframework.org/spring-ws";
		this.localPart = "element";

		MockWebServiceMessage request = new MockWebServiceMessage(
				"<" + this.localPart + " xmlns=\"" + this.namespaceUri + "\" />");
		this.messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
	}

	@Test
	void neitherNamespaceNorLocalPart() {

		assertThatIllegalArgumentException()
			.isThrownBy(() -> new PayloadRootSmartSoapEndpointInterceptor(this.delegate, null, null));
	}

	@Test
	void shouldInterceptFullMatch() {

		PayloadRootSmartSoapEndpointInterceptor interceptor = new PayloadRootSmartSoapEndpointInterceptor(this.delegate,
				this.namespaceUri, this.localPart);

		boolean result = interceptor.shouldIntercept(this.messageContext, null);

		assertThat(result).isTrue();
	}

	@Test
	void shouldInterceptFullNonMatch() {

		PayloadRootSmartSoapEndpointInterceptor interceptor = new PayloadRootSmartSoapEndpointInterceptor(this.delegate,
				"http://springframework.org/other", this.localPart);

		boolean result = interceptor.shouldIntercept(this.messageContext, null);

		assertThat(result).isFalse();
	}

	@Test
	void shouldInterceptNamespaceUriMatch() {

		PayloadRootSmartSoapEndpointInterceptor interceptor = new PayloadRootSmartSoapEndpointInterceptor(this.delegate,
				this.namespaceUri, null);

		boolean result = interceptor.shouldIntercept(this.messageContext, null);

		assertThat(result).isTrue();
	}

	@Test
	void shouldInterceptNamespaceUriNonMatch() {

		PayloadRootSmartSoapEndpointInterceptor interceptor = new PayloadRootSmartSoapEndpointInterceptor(this.delegate,
				"http://springframework.org/other", null);

		boolean result = interceptor.shouldIntercept(this.messageContext, null);

		assertThat(result).isFalse();
	}

}
