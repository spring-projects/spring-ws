/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractSoapEnvelopeTests extends AbstractSoapElementTests {

	protected SoapEnvelope soapEnvelope;

	@Override
	protected final SoapElement createSoapElement() throws Exception {

		this.soapEnvelope = createSoapEnvelope();
		return this.soapEnvelope;
	}

	protected abstract SoapEnvelope createSoapEnvelope() throws Exception;

	@Test
	void testGetHeader() {

		SoapHeader header = this.soapEnvelope.getHeader();

		assertThat(header).isNotNull();
	}

	@Test
	void testGetBody() {

		SoapBody body = this.soapEnvelope.getBody();

		assertThat(body).isNotNull();
	}

}
