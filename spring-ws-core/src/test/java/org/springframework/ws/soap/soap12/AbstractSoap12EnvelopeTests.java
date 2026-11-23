/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
