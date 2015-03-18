/*
 * Copyright 2005-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.server.endpoint.interceptor;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PayloadRootSmartSoapEndpointInterceptorTest {

	private EndpointInterceptor delegate;

	private String namespaceUri;

	private String localPart;

	private MessageContext messageContext;

	@Before
	public void setUp() {
		delegate = new EndpointInterceptorAdapter();

		namespaceUri = "http://springframework.org/spring-ws";
		localPart = "element";

		MockWebServiceMessage request = new MockWebServiceMessage("<" + localPart + " xmlns=\"" + namespaceUri + "\" />");
		messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
	}

	@Test(expected = IllegalArgumentException.class)
	public void neitherNamespaceNorLocalPart() {
		new PayloadRootSmartSoapEndpointInterceptor(delegate, null, null);
	}

	@Test
	public void shouldInterceptFullMatch() throws Exception {
		PayloadRootSmartSoapEndpointInterceptor interceptor =
				new PayloadRootSmartSoapEndpointInterceptor(delegate, namespaceUri, localPart);

		boolean result = interceptor.shouldIntercept(messageContext, null);
		assertTrue("Interceptor should apply", result);
	}

	@Test
	public void shouldInterceptFullNonMatch() throws Exception {
		PayloadRootSmartSoapEndpointInterceptor interceptor =
				new PayloadRootSmartSoapEndpointInterceptor(delegate, "http://springframework.org/other", localPart);

		boolean result = interceptor.shouldIntercept(messageContext, null);
		assertFalse("Interceptor should not apply", result);
	}

	@Test
	public void shouldInterceptNamespaceUriMatch() throws Exception {
		PayloadRootSmartSoapEndpointInterceptor interceptor =
				new PayloadRootSmartSoapEndpointInterceptor(delegate, namespaceUri, null);

		boolean result = interceptor.shouldIntercept(messageContext, null);
		assertTrue("Interceptor should apply", result);
	}
	
	@Test
	public void shouldInterceptNamespaceUriNonMatch() throws Exception {
		PayloadRootSmartSoapEndpointInterceptor interceptor =
				new PayloadRootSmartSoapEndpointInterceptor(delegate, "http://springframework.org/other", null);

		boolean result = interceptor.shouldIntercept(messageContext, null);
		assertFalse("Interceptor should not apply", result);
	}

}
