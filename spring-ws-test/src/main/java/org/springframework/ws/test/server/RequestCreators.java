/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.server;

import java.io.IOException;

import javax.xml.transform.Source;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.test.support.creator.PayloadMessageCreator;
import org.springframework.ws.test.support.creator.SoapEnvelopeMessageCreator;
import org.springframework.ws.test.support.creator.WebServiceMessageCreator;
import org.springframework.xml.transform.ResourceSource;

/**
 * Factory methods for {@link RequestCreator} classes. Typically used to provide input for
 * {@link MockWebServiceClient#sendRequest(RequestCreator)}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public abstract class RequestCreators {

	private RequestCreators() {
	}

	// Payload

	/**
	 * Create a request with the given {@link Source} XML as payload.
	 * @param payload the request payload
	 * @return the request creator
	 */
	public static RequestCreator withPayload(Source payload) {
		Assert.notNull(payload, "'payload' must not be null");
		return new WebServiceMessageCreatorAdapter(new PayloadMessageCreator(payload));
	}

	/**
	 * Create a request with the given {@link Resource} XML as payload.
	 * @param payload the request payload
	 * @return the request creator
	 */
	public static RequestCreator withPayload(Resource payload) throws IOException {
		Assert.notNull(payload, "'payload' must not be null");
		return withPayload(new ResourceSource(payload));
	}

	// SOAP

	/**
	 * Create a request with the given {@link Source} XML as SOAP envelope.
	 * @param soapEnvelope the request SOAP envelope
	 * @return the request creator
	 * @since 2.1.1
	 */
	public static RequestCreator withSoapEnvelope(Source soapEnvelope) {
		Assert.notNull(soapEnvelope, "'soapEnvelope' must not be null");
		return new WebServiceMessageCreatorAdapter(new SoapEnvelopeMessageCreator(soapEnvelope));
	}

	/**
	 * Create a request with the given {@link Resource} XML as SOAP envelope.
	 * @param soapEnvelope the request SOAP envelope
	 * @return the request creator
	 * @since 2.1.1
	 */
	public static RequestCreator withSoapEnvelope(Resource soapEnvelope) throws IOException {
		Assert.notNull(soapEnvelope, "'soapEnvelope' must not be null");
		return withSoapEnvelope(new ResourceSource(soapEnvelope));
	}

	/**
	 * Adapts a {@link WebServiceMessageCreator} to the {@link RequestCreator} contract.
	 */
	private static final class WebServiceMessageCreatorAdapter implements RequestCreator {

		private final WebServiceMessageCreator adaptee;

		private WebServiceMessageCreatorAdapter(WebServiceMessageCreator adaptee) {
			this.adaptee = adaptee;
		}

		@Override
		public WebServiceMessage createRequest(WebServiceMessageFactory messageFactory) throws IOException {
			return this.adaptee.createMessage(messageFactory);
		}

	}

}
