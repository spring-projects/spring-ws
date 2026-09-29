/*
 * Copyright 2005-present the original author or authors.
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

package org.springframework.ws.soap.security;

import java.io.IOException;
import java.io.InputStream;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AbstractWsSecurityInterceptor}.
 *
 * @author Stephane Nicoll
 */
class AbstractWsSecurityInterceptorTests {

	private MessageFactory messageFactory;

	private SaajSoapMessageFactory soapMessageFactory;

	@BeforeEach
	void setUp() throws Exception {
		this.messageFactory = MessageFactory.newInstance();
		this.soapMessageFactory = new SaajSoapMessageFactory(this.messageFactory);
	}

	@Test
	void faultDoesNotRevealWhyValidationFailed() throws Exception {
		String faultString = faultStringFor("Cannot decrypt the encrypted key: "
				+ "Given final block not properly padded, using alias serverAlias");
		assertThat(faultString).isEqualTo(AbstractWsSecurityInterceptor.VALIDATION_FAULT_STRING);
		assertThat(faultString).doesNotContain("padded", "decrypt", "serverAlias");
	}

	@Test
	void faultIsTheSameWhateverValidationFailure() throws Exception {
		assertThat(faultStringFor("Given final block not properly padded"))
			.isEqualTo(faultStringFor("Error parsing the inputstream"))
			.isEqualTo(faultStringFor("The security token could not be authenticated or authorized"))
			.isEqualTo(AbstractWsSecurityInterceptor.VALIDATION_FAULT_STRING);
	}

	private String faultStringFor(String validationFailureMessage) throws Exception {
		AbstractWsSecurityInterceptor interceptor = new FailingWsSecurityInterceptor(validationFailureMessage);
		MessageContext messageContext = new DefaultMessageContext(loadSaajMessage("noHeader-soap.xml"),
				this.soapMessageFactory);
		assertThat(interceptor.handleRequest(messageContext, null)).isFalse();
		return ((SoapMessage) messageContext.getResponse()).getSoapBody().getFault().getFaultStringOrReason();
	}

	private SaajSoapMessage loadSaajMessage(String fileName) throws SOAPException, IOException {
		MimeHeaders mimeHeaders = new MimeHeaders();
		mimeHeaders.addHeader("Content-Type", "text/xml");
		Resource resource = new ClassPathResource(fileName, getClass());
		assertThat(resource.exists()).isTrue();
		try (InputStream is = resource.getInputStream()) {
			return new SaajSoapMessage(this.messageFactory.createMessage(mimeHeaders, is));
		}
	}

	static class FailingWsSecurityInterceptor extends AbstractWsSecurityInterceptor {

		private final String validationFailureMessage;

		FailingWsSecurityInterceptor(String validationFailureMessage) {
			this.validationFailureMessage = validationFailureMessage;
		}

		protected void validateMessage(SoapMessage soapMessage, MessageContext messageContext)
				throws WsSecurityValidationException {
			throw new WsSecurityValidationException(this.validationFailureMessage) {
			};
		}

		@Override
		protected void secureMessage(SoapMessage soapMessage, MessageContext messageContext)
				throws WsSecuritySecurementException {
		}

		@Override
		protected void cleanUp() {
		}

	}

}
