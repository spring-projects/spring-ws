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

package org.springframework.ws.soap.saaj.test;

import java.io.IOException;
import java.io.InputStream;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPException;

import org.springframework.core.io.Resource;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

/**
 * Utility class for handing SAAJ SOAP messages.
 *
 * @author Stephane Nicoll
 * @since 3.1.9
 */
public abstract class SaajSoapMessages {

	private static final MessageFactory messageFactory = createSaaj12MessageFactory();

	public static MessageFactory messageFactory() {
		return messageFactory;
	}

	/**
	 * Load a SAAJ SOAP message from the given resource.
	 * @param resource the location of the message
	 * @return a SAAJ SOAP message with the content of the given resource
	 */
	public static SaajSoapMessage load(Resource resource) {
		MimeHeaders mimeHeaders = new MimeHeaders();
		mimeHeaders.addHeader("Content-Type", " application/soap+xml");
		try (InputStream is = resource.getInputStream()) {
			return new SaajSoapMessage(messageFactory.createMessage(mimeHeaders, is));
		}
		catch (SOAPException | IOException ex) {
			throw new IllegalStateException("Failed to load message " + resource, ex);
		}
	}

	/**
	 * Create a {@link MessageContext} for the given request.
	 * @param request the resource containing the SAAJ SOAP message containing the request
	 * @return a message context for the given request
	 */
	public static DefaultMessageContext createMessageContext(Resource request) {
		SaajSoapMessage message = load(request);
		return new DefaultMessageContext(message, new SaajSoapMessageFactory(SaajSoapMessages.messageFactory()));
	}

	private static MessageFactory createSaaj12MessageFactory() {
		try {
			return MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		}
		catch (SOAPException ex) {
			throw new IllegalStateException("Failed to create SAAJ 1.2 MessageFactory", ex);
		}
	}

}
