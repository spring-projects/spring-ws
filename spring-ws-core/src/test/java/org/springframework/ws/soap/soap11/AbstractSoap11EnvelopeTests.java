/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.soap11;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.ws.soap.AbstractSoapEnvelopeTests;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.xml.transform.StringResult;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractSoap11EnvelopeTests extends AbstractSoapEnvelopeTests {

	@Test
	void testGetName() {
		assertThat(this.soapEnvelope.getName())
			.isEqualTo(new QName(SoapVersion.SOAP_11.getEnvelopeNamespaceUri(), "Envelope"));
	}

	@Test
	void testGetContent() throws Exception {

		StringResult result = new StringResult();
		this.transformer.transform(this.soapEnvelope.getSource(), result);

		XmlAssert.assertThat(result.toString())
			.and("<Envelope xmlns='http://schemas.xmlsoap.org/soap/envelope/'><Header/><Body/></Envelope>")
			.ignoreWhitespace()
			.areSimilar();
	}

}
