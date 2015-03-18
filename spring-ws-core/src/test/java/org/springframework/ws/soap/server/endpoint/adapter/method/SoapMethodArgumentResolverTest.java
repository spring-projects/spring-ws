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

package org.springframework.ws.soap.server.endpoint.adapter.method;

import org.springframework.core.MethodParameter;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.adapter.method.AbstractMethodArgumentResolverTestCase;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** @author Arjen Poutsma */
public class SoapMethodArgumentResolverTest extends AbstractMethodArgumentResolverTestCase {

	private SoapMethodArgumentResolver resolver;

	private MethodParameter soapMessageParameter;

	private MethodParameter soapEnvelopeParameter;

	private MethodParameter soapBodyParameter;

	private MethodParameter soapHeaderParameter;

	@Before
	public void setUp() throws Exception {
		resolver = new SoapMethodArgumentResolver();
		soapMessageParameter = new MethodParameter(getClass().getMethod("soapMessage", SoapMessage.class), 0);
		soapEnvelopeParameter = new MethodParameter(getClass().getMethod("soapEnvelope", SoapEnvelope.class), 0);
		soapBodyParameter = new MethodParameter(getClass().getMethod("soapBody", SoapBody.class), 0);
		soapHeaderParameter = new MethodParameter(getClass().getMethod("soapHeader", SoapHeader.class), 0);
	}

	@Test
	public void supportsParameter() throws Exception {
		assertTrue("resolver does not support SoapMessage", resolver.supportsParameter(soapMessageParameter));
		assertTrue("resolver does not support SoapEnvelope", resolver.supportsParameter(soapEnvelopeParameter));
		assertTrue("resolver does not support SoapBody", resolver.supportsParameter(soapBodyParameter));
		assertTrue("resolver does not support SoapHeader", resolver.supportsParameter(soapHeaderParameter));
	}

	@Test
	public void resolveSoapMessageSaaj() throws Exception {
		MessageContext messageContext = createSaajMessageContext();

		Object result = resolver.resolveArgument(messageContext, soapMessageParameter);

		assertEquals(messageContext.getRequest(), result);
	}

	@Test
	public void resolveSoapEnvelopeSaaj() throws Exception {
		MessageContext messageContext = createSaajMessageContext();

		Object result = resolver.resolveArgument(messageContext, soapEnvelopeParameter);

		assertEquals(((SoapMessage)messageContext.getRequest()).getEnvelope(), result);
	}

	@Test
	public void resolveSoapBodySaaj() throws Exception {
		MessageContext messageContext = createSaajMessageContext();

		Object result = resolver.resolveArgument(messageContext, soapBodyParameter);

		assertEquals(((SoapMessage)messageContext.getRequest()).getSoapBody(), result);
	}

	@Test
	public void resolveSoapHeaderSaaj() throws Exception {
		MessageContext messageContext = createSaajMessageContext();

		Object result = resolver.resolveArgument(messageContext, soapHeaderParameter);

		assertEquals(((SoapMessage)messageContext.getRequest()).getSoapHeader(), result);
	}

	public void soapMessage(SoapMessage soapMessage) {

	}

	public void soapEnvelope(SoapEnvelope soapEnvelope) {
	}

	public void soapBody(SoapBody soapBody) {
	}

	public void soapHeader(SoapHeader soapHeader) {
	}
}
