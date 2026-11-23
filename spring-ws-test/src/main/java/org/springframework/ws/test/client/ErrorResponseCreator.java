/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.client;

import java.io.IOException;
import java.net.URI;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

/**
 * Implementation of {@link ResponseCreator} that holds an error message.
 *
 * @author Arjen Poutsma
 * @author Lukas Krecan
 * @since 2.0
 */
class ErrorResponseCreator implements ResponseCreator {

	private final String errorMessage;

	ErrorResponseCreator(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public WebServiceMessage createResponse(URI uri, WebServiceMessage request, WebServiceMessageFactory factory)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	String getErrorMessage() {
		return this.errorMessage;
	}

}
