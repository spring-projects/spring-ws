/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap;

import org.junit.jupiter.api.Test;

import org.springframework.ws.AbstractWebServiceMessageFactoryTests;
import org.springframework.ws.InvalidXmlException;
import org.springframework.ws.WebServiceMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public abstract class AbstractSoapMessageFactoryTests extends AbstractWebServiceMessageFactoryTests {

	@Test
	void testCreateEmptySoapMessage() {

		WebServiceMessage message = this.messageFactory.createWebServiceMessage();

		assertThat(message).isInstanceOf(SoapMessage.class);
	}

	@Test
	void testCreateSoapMessageIllFormedXml() {
		assertThatExceptionOfType(InvalidXmlException.class).isThrownBy(this::doTestCreateSoapMessageIllFormedXml);
	}

	protected abstract void doTestCreateSoapMessageIllFormedXml() throws Exception;

	@Test
	protected abstract void testCreateSoapMessageNoAttachment() throws Exception;

	@Test
	protected abstract void testCreateSoapMessageSwA() throws Exception;

	@Test
	protected abstract void testCreateSoapMessageMtom() throws Exception;

	@Test
	protected abstract void testCreateSoapMessageMissingContentType() throws Exception;

}
