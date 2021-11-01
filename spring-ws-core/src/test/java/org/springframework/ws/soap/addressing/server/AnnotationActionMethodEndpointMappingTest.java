/*
 * Copyright 2005-2014 the original author or authors.
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

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPException;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.interceptor.DelegatingSmartEndpointInterceptor;
import org.springframework.ws.server.endpoint.interceptor.PayloadLoggingInterceptor;
import org.springframework.ws.soap.addressing.server.annotation.Action;
import org.springframework.ws.soap.addressing.server.annotation.Address;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

/**
 * @author Arjen Poutsma
 * @author Nate Stoddard
 */
public class AnnotationActionMethodEndpointMappingTest {

	private StaticApplicationContext applicationContext;

	private AnnotationActionEndpointMapping mapping;

	private MessageFactory messageFactory;

	@BeforeEach
	public void setUp() throws Exception {

		messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);

		applicationContext = new StaticApplicationContext();
		applicationContext.registerSingleton("mapping", AnnotationActionEndpointMapping.class);
		applicationContext.registerSingleton("interceptor", MyInterceptor.class);
		applicationContext.registerSingleton("smartIntercepter", MySmartInterceptor.class);
		applicationContext.registerSingleton("endpoint", MyEndpoint.class);
		applicationContext.refresh();

		mapping = (AnnotationActionEndpointMapping) applicationContext.getBean("mapping");
	}

	@Test
	public void mapping() throws Exception {

		MessageContext messageContext = createMessageContext();

		EndpointInvocationChain chain = mapping.getEndpoint(messageContext);

		assertThat(chain).isNotNull();

		MethodEndpoint expected = new MethodEndpoint(applicationContext.getBean("endpoint"), "doIt");

		assertThat(chain.getEndpoint()).isEqualTo(expected);
		assertThat(chain.getInterceptors()).hasSize(2);
		assertThat(chain.getInterceptors()[0]).isInstanceOf(AddressingEndpointInterceptor.class);
		assertThat(chain.getInterceptors()[1]).isInstanceOf(MyInterceptor.class);
	}

	private MessageContext createMessageContext() throws SOAPException, IOException {

		MimeHeaders mimeHeaders = new MimeHeaders();
		mimeHeaders.addHeader("Content-Type", " application/soap+xml");
		InputStream is = getClass().getResourceAsStream("valid.xml");

		assertThat(is).isNotNull();

		try {
			SaajSoapMessage message = new SaajSoapMessage(messageFactory.createMessage(mimeHeaders, is));
			return new DefaultMessageContext(message, new SaajSoapMessageFactory(messageFactory));
		} finally {
			is.close();
		}
	}

	@Endpoint
	@Address("mailto:joe@fabrikam123.example")
	private static class MyEndpoint {

		@Action("http://fabrikam123.example/mail/Delete")
		public void doIt() {

		}
	}

	private static class MyInterceptor extends DelegatingSmartEndpointInterceptor {

		public MyInterceptor() {
			super(new PayloadLoggingInterceptor());
		}
	}

	private static class MySmartInterceptor extends DelegatingSmartEndpointInterceptor {

		public MySmartInterceptor() {
			super(new PayloadLoggingInterceptor());
		}

		public boolean shouldIntercept(MessageContext messageContext, Object endpoint) {
			return false;
		}
	}
}
