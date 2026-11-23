/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client.support.interceptor;

import org.xml.sax.SAXParseException;

import org.springframework.ws.client.WebServiceClientException;

/**
 * Exception thrown whenever a validation error occurs on the client-side.
 *
 * @author Stefan Schmidt
 * @author Arjen Poutsma
 * @author Stephane Nicoll
 * @since 1.5.4
 */
@SuppressWarnings("serial")
public class WebServiceValidationException extends WebServiceClientException {

	private final SAXParseException[] validationErrors;

	/**
	 * Create a new instance of the {@code WebServiceValidationException} class.
	 * @deprecated as of 3.1.10 in favor of
	 * {@link WebServiceValidationException#WebServiceValidationException(ValidatedMessage, SAXParseException[])}
	 */
	@Deprecated
	public WebServiceValidationException(SAXParseException[] validationErrors) {
		this(ValidatedMessage.REQUEST, validationErrors);
	}

	/**
	 * Create a new instance of the {@code WebServiceValidationException} class for
	 * validation errors found in the given message.
	 * @param validatedMessage the message that failed validation
	 * @param validationErrors the validation errors
	 * @since 3.1.10
	 */
	public WebServiceValidationException(ValidatedMessage validatedMessage, SAXParseException[] validationErrors) {
		super(createMessage(validatedMessage, validationErrors));
		this.validationErrors = validationErrors;
	}

	private static String createMessage(ValidatedMessage validatedMessage, SAXParseException[] validationErrors) {
		StringBuilder builder = new StringBuilder("XML validation error on ");
		builder.append(validatedMessage.description).append(": ");

		for (SAXParseException validationError : validationErrors) {
			builder.append(validationError.getMessage());
		}
		return builder.toString();
	}

	/**
	 * Return the validation errors.
	 */
	public SAXParseException[] getValidationErrors() {
		return this.validationErrors;
	}

	/**
	 * The message of an exchange that failed validation.
	 *
	 * @since 3.1.10
	 */
	public enum ValidatedMessage {

		/**
		 * The request that was about to be sent.
		 */
		REQUEST("request"),

		/**
		 * The response that was received.
		 */
		RESPONSE("response");

		private final String description;

		ValidatedMessage(String description) {
			this.description = description;
		}

	}

}
