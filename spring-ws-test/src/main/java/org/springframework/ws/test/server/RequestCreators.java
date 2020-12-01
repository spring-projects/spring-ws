/*
 * Copyright 2005-2014 the original author or authors.
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

	private RequestCreators() {}

	// Payload

	/**
	 * Create a request with the given {@link Source} XML as payload.
	 *
	 * @param payload the request payload
	 * @return the request creator
	 */
	public static RequestCreator withPayload(Source payload) {
		Assert.notNull(payload, "'payload' must not be null");
		return new WebServiceMessageCreatorAdapter(new PayloadMessageCreator(payload));
	}

	/**
	 * Create a request with the given {@link Resource} XML as payload.
	 *
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
	 *
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
	 *
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
	private static class WebServiceMessageCreatorAdapter implements RequestCreator {

		private final WebServiceMessageCreator adaptee;

		private WebServiceMessageCreatorAdapter(WebServiceMessageCreator adaptee) {
			this.adaptee = adaptee;
		}

		@Override
		public WebServiceMessage createRequest(WebServiceMessageFactory messageFactory) throws IOException {
			return adaptee.createMessage(messageFactory);
		}
	}

}
