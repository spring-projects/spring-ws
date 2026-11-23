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
 * Implementation of {@link ResponseCreator} that responds by throwing either an
 * {@link IOException} or a {@link RuntimeException}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
class ExceptionResponseCreator implements ResponseCreator {

	private final Exception exception;

	ExceptionResponseCreator(IOException exception) {
		this.exception = exception;
	}

	ExceptionResponseCreator(RuntimeException exception) {
		this.exception = exception;
	}

	@Override
	public WebServiceMessage createResponse(URI uri, WebServiceMessage request, WebServiceMessageFactory factory)
			throws IOException {
		if (this.exception instanceof IOException) {
			throw (IOException) this.exception;
		}
		else {
			throw (RuntimeException) this.exception;
		}
	}

}
