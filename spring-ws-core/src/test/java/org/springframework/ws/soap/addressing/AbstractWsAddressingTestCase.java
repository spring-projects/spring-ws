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

package org.springframework.ws.soap.addressing;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.w3c.dom.Document;
import org.xmlunit.assertj.XmlAssert;

public abstract class AbstractWsAddressingTestCase {

	protected MessageFactory messageFactory;

	@BeforeEach
	public void createMessageFactory() throws Exception {
		messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
	}

	protected SaajSoapMessage loadSaajMessage(String fileName) throws SOAPException, IOException {

		MimeHeaders mimeHeaders = new MimeHeaders();
		mimeHeaders.addHeader("Content-Type", " application/soap+xml");
		InputStream is = AbstractWsAddressingTestCase.class.getResourceAsStream(fileName);

		assertThat(is).isNotNull();

		try {
			return new SaajSoapMessage(messageFactory.createMessage(mimeHeaders, is));
		} finally {
			is.close();
		}
	}

	protected void assertXMLSimilar(SaajSoapMessage expected, SaajSoapMessage result) {

		Document expectedDocument = expected.getSaajMessage().getSOAPPart();
		Document resultDocument = result.getSaajMessage().getSOAPPart();

		XmlAssert.assertThat(resultDocument).and(expectedDocument).ignoreWhitespace().areSimilar();
	}

	protected void assertXMLNotSimilar(SaajSoapMessage expected, SaajSoapMessage result) {

		Document expectedDocument = expected.getSaajMessage().getSOAPPart();
		Document resultDocument = result.getSaajMessage().getSOAPPart();

		XmlAssert.assertThat(resultDocument).and(expectedDocument).ignoreWhitespace().areNotSimilar();
	}
}
