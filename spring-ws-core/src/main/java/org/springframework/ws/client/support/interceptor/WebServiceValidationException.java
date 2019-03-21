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

package org.springframework.ws.client.support.interceptor;

import org.xml.sax.SAXParseException;

import org.springframework.ws.client.WebServiceClientException;

/**
 * Exception thrown whenever a validation error occurs on the client-side.
 *
 * @author Stefan Schmidt
 * @author Arjen Poutsma
 * @since 1.5.4
 */
@SuppressWarnings("serial")
public class WebServiceValidationException extends WebServiceClientException {

	private SAXParseException[] validationErrors;

	/**
	 * Create a new instance of the {@code WebServiceValidationException} class.
	 */
	public WebServiceValidationException(SAXParseException[] validationErrors) {
		super(createMessage(validationErrors));
		this.validationErrors = validationErrors;
	}

	private static String createMessage(SAXParseException[] validationErrors) {
		StringBuilder builder = new StringBuilder("XML validation error on response: ");

		for (SAXParseException validationError : validationErrors) {
			builder.append(validationError.getMessage());
		}
		return builder.toString();
	}

	/** Returns the validation errors. */
	public SAXParseException[] getValidationErrors() {
		return validationErrors;
	}
}
