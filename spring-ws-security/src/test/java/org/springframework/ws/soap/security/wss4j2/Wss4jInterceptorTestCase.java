/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.soap.security.wss4j2;

import static org.assertj.core.api.Assertions.*;

import org.apache.wss4j.dom.engine.WSSecurityEngine;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.WsSecuritySecurementException;
import org.springframework.ws.soap.security.WsSecurityValidationException;

public abstract class Wss4jInterceptorTestCase extends Wss4jTestCase {

	@Test
	public void testHandleRequest() throws Exception {

		SoapMessage request = loadSoap11Message("empty-soap.xml");
		final Object requestMessage = getMessage(request);
		SoapMessage validatedRequest = loadSoap11Message("empty-soap.xml");
		final Object validatedRequestMessage = getMessage(validatedRequest);

		Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor() {

			@Override
			protected void secureMessage(SoapMessage soapMessage, MessageContext messageContext)
					throws WsSecuritySecurementException {
				fail("secure not expected");
			}

			@Override
			protected void validateMessage(SoapMessage soapMessage, MessageContext messageContext)
					throws WsSecurityValidationException {

				assertThat(getMessage(soapMessage)).isEqualTo(requestMessage);
				setMessage(soapMessage, validatedRequestMessage);
			}
		};

		MessageContext context = new DefaultMessageContext(request, getSoap11MessageFactory());
		interceptor.handleRequest(context, null);

		assertThat(getMessage((SoapMessage) context.getRequest())).isEqualTo(validatedRequestMessage);
	}

	@Test
	public void testHandleResponse() throws Exception {

		SoapMessage securedResponse = loadSoap11Message("empty-soap.xml");
		final Object securedResponseMessage = getMessage(securedResponse);

		Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor() {

			@Override
			protected void secureMessage(SoapMessage soapMessage, MessageContext messageContext)
					throws WsSecuritySecurementException {
				setMessage(soapMessage, securedResponseMessage);
			}

			@Override
			protected void validateMessage(SoapMessage soapMessage, MessageContext messageContext)
					throws WsSecurityValidationException {
				fail("validate not expected");
			}

		};

		SoapMessage request = loadSoap11Message("empty-soap.xml");
		MessageContext context = new DefaultMessageContext(request, getSoap11MessageFactory());
		context.getResponse();
		interceptor.handleResponse(context, null);

		assertThat(getMessage((SoapMessage) context.getResponse())).isEqualTo(securedResponseMessage);
	}

	@Test
	public void testHandleCustomSecurityEngine() {

		WSSecurityEngine engine = new WSSecurityEngine();
		Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor(engine);

		assertThat(ReflectionTestUtils.getField(interceptor, "securityEngine")).isEqualTo(engine);
	}
}
