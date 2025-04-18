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

public class PayloadRootSmartSoapEndpointInterceptorTests {

	private EndpointInterceptor delegate;

	private String namespaceUri;

	private String localPart;

	private MessageContext messageContext;

	@BeforeEach
	public void setUp() {

		this.delegate = new EndpointInterceptorAdapter();

		this.namespaceUri = "http://springframework.org/spring-ws";
		this.localPart = "element";

		MockWebServiceMessage request = new MockWebServiceMessage(
				"<" + this.localPart + " xmlns=\"" + this.namespaceUri + "\" />");
		this.messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
	}

	@Test
	public void neitherNamespaceNorLocalPart() {

		assertThatIllegalArgumentException()
			.isThrownBy(() -> new PayloadRootSmartSoapEndpointInterceptor(this.delegate, null, null));
	}

	@Test
	public void shouldInterceptFullMatch() {

		PayloadRootSmartSoapEndpointInterceptor interceptor = new PayloadRootSmartSoapEndpointInterceptor(this.delegate,
				this.namespaceUri, this.localPart);

		boolean result = interceptor.shouldIntercept(this.messageContext, null);

		assertThat(result).isTrue();
	}

	@Test
	public void shouldInterceptFullNonMatch() {

		PayloadRootSmartSoapEndpointInterceptor interceptor = new PayloadRootSmartSoapEndpointInterceptor(this.delegate,
				"http://springframework.org/other", this.localPart);

		boolean result = interceptor.shouldIntercept(this.messageContext, null);

		assertThat(result).isFalse();
	}

	@Test
	public void shouldInterceptNamespaceUriMatch() {

		PayloadRootSmartSoapEndpointInterceptor interceptor = new PayloadRootSmartSoapEndpointInterceptor(this.delegate,
				this.namespaceUri, null);

		boolean result = interceptor.shouldIntercept(this.messageContext, null);

		assertThat(result).isTrue();
	}

	@Test
	public void shouldInterceptNamespaceUriNonMatch() {

		PayloadRootSmartSoapEndpointInterceptor interceptor = new PayloadRootSmartSoapEndpointInterceptor(this.delegate,
				"http://springframework.org/other", null);

		boolean result = interceptor.shouldIntercept(this.messageContext, null);

		assertThat(result).isFalse();
	}

}
