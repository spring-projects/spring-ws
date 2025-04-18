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

package org.springframework.ws.test.support.matcher;

import javax.xml.namespace.QName;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.easymock.EasyMock.createMock;

public class SoapHeaderMatcherTests {

	private SoapHeaderMatcher matcher;

	private QName expectedHeaderName;

	@BeforeEach
	public void setUp() {

		this.expectedHeaderName = new QName("http://example.com", "header");
		this.matcher = new SoapHeaderMatcher(this.expectedHeaderName);
	}

	@Test
	public void match() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage saajMessage = messageFactory.createMessage();
		saajMessage.getSOAPHeader().addHeaderElement(this.expectedHeaderName);
		SoapMessage soapMessage = new SaajSoapMessage(saajMessage);

		this.matcher.match(soapMessage);
	}

	@Test
	public void nonMatch() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage saajMessage = messageFactory.createMessage();
			SoapMessage soapMessage = new SaajSoapMessage(saajMessage);

			this.matcher.match(soapMessage);
		});
	}

	@Test
	public void nonSoap() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			WebServiceMessage message = createMock(WebServiceMessage.class);

			this.matcher.match(message);
		});
	}

}
