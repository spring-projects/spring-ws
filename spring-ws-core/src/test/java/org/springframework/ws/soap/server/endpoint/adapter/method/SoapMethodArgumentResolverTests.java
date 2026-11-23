/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.server.endpoint.adapter.method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.MethodParameter;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.adapter.method.AbstractMethodArgumentResolverTests;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arjen Poutsma
 */
class SoapMethodArgumentResolverTests extends AbstractMethodArgumentResolverTests {

	private SoapMethodArgumentResolver resolver;

	private MethodParameter soapMessageParameter;

	private MethodParameter soapEnvelopeParameter;

	private MethodParameter soapBodyParameter;

	private MethodParameter soapHeaderParameter;

	@BeforeEach
	void setUp() throws Exception {

		this.resolver = new SoapMethodArgumentResolver();
		this.soapMessageParameter = new MethodParameter(getClass().getMethod("soapMessage", SoapMessage.class), 0);
		this.soapEnvelopeParameter = new MethodParameter(getClass().getMethod("soapEnvelope", SoapEnvelope.class), 0);
		this.soapBodyParameter = new MethodParameter(getClass().getMethod("soapBody", SoapBody.class), 0);
		this.soapHeaderParameter = new MethodParameter(getClass().getMethod("soapHeader", SoapHeader.class), 0);
	}

	@Test
	void supportsParameter() {

		assertThat(this.resolver.supportsParameter(this.soapMessageParameter)).isTrue();
		assertThat(this.resolver.supportsParameter(this.soapEnvelopeParameter)).isTrue();
		assertThat(this.resolver.supportsParameter(this.soapBodyParameter)).isTrue();
		assertThat(this.resolver.supportsParameter(this.soapHeaderParameter)).isTrue();
	}

	@Test
	void resolveSoapMessageSaaj() throws Exception {

		MessageContext messageContext = createSaajMessageContext();
		Object result = this.resolver.resolveArgument(messageContext, this.soapMessageParameter);

		assertThat(result).isEqualTo(messageContext.getRequest());
	}

	@Test
	void resolveSoapEnvelopeSaaj() throws Exception {

		MessageContext messageContext = createSaajMessageContext();
		Object result = this.resolver.resolveArgument(messageContext, this.soapEnvelopeParameter);

		assertThat(result).isEqualTo(((SoapMessage) messageContext.getRequest()).getEnvelope());
	}

	@Test
	void resolveSoapBodySaaj() throws Exception {

		MessageContext messageContext = createSaajMessageContext();
		Object result = this.resolver.resolveArgument(messageContext, this.soapBodyParameter);

		assertThat(result).isEqualTo(((SoapMessage) messageContext.getRequest()).getSoapBody());
	}

	@Test
	void resolveSoapHeaderSaaj() throws Exception {

		MessageContext messageContext = createSaajMessageContext();
		Object result = this.resolver.resolveArgument(messageContext, this.soapHeaderParameter);

		assertThat(result).isEqualTo(((SoapMessage) messageContext.getRequest()).getSoapHeader());
	}

	public void soapMessage(SoapMessage soapMessage) {

	}

	public void soapEnvelope(SoapEnvelope soapEnvelope) {
	}

	public void soapBody(SoapBody soapBody) {
	}

	public void soapHeader(SoapHeader soapHeader) {
	}

}
