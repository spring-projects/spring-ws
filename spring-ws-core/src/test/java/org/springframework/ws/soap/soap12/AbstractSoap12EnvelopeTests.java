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

package org.springframework.ws.soap.soap12;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.ws.soap.AbstractSoapEnvelopeTests;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.xml.transform.StringResult;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractSoap12EnvelopeTests extends AbstractSoapEnvelopeTests {

	@Test
	void testGetName() {
		assertThat(this.soapEnvelope.getName())
			.isEqualTo(new QName(SoapVersion.SOAP_12.getEnvelopeNamespaceUri(), "Envelope"));
	}

	@Test
	void testGetSource() throws Exception {

		StringResult result = new StringResult();
		this.transformer.transform(this.soapEnvelope.getSource(), result);

		XmlAssert.assertThat(result.toString())
			.and("<Envelope xmlns='http://www.w3.org/2003/05/soap-envelope'><Header/>" + "<Body/></Envelope>")
			.ignoreWhitespace()
			.areSimilar();
	}

}
