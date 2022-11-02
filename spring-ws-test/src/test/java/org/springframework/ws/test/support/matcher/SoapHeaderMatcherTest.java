/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ws.test.support.matcher;

import static org.assertj.core.api.Assertions.*;
import static org.easymock.EasyMock.*;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPMessage;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

public class SoapHeaderMatcherTest {

	private SoapHeaderMatcher matcher;

	private QName expectedHeaderName;

	@BeforeEach
	public void setUp() {

		expectedHeaderName = new QName("http://example.com", "header");
		matcher = new SoapHeaderMatcher(expectedHeaderName);
	}

	@Test
	public void match() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage saajMessage = messageFactory.createMessage();
		saajMessage.getSOAPHeader().addHeaderElement(expectedHeaderName);
		SoapMessage soapMessage = new SaajSoapMessage(saajMessage);

		matcher.match(soapMessage);
	}

	@Test
	public void nonMatch() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage saajMessage = messageFactory.createMessage();
			SoapMessage soapMessage = new SaajSoapMessage(saajMessage);

			matcher.match(soapMessage);
		});
	}

	@Test
	public void nonSoap() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			WebServiceMessage message = createMock(WebServiceMessage.class);

			matcher.match(message);
		});
	}

}
