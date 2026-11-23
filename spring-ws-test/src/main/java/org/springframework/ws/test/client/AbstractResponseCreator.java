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
 * Abstract base class for the {@link ResponseCreator} interface.
 * <p>
 * Creates a response using the given {@link WebServiceMessageFactory}, and passes it on
 * to {@link #doWithResponse(URI, WebServiceMessage, WebServiceMessage)}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
abstract class AbstractResponseCreator implements ResponseCreator {

	@Override
	public final WebServiceMessage createResponse(URI uri, WebServiceMessage request,
			WebServiceMessageFactory messageFactory) throws IOException {
		WebServiceMessage response = messageFactory.createWebServiceMessage();
		doWithResponse(uri, request, response);
		return response;
	}

	/**
	 * Execute any number of operations on the supplied response, given the request and
	 * URI.
	 * @param uri the URI
	 * @param request the request message
	 * @param response the response message
	 * @throws IOException in case of I/O errors
	 */
	protected abstract void doWithResponse(URI uri, WebServiceMessage request, WebServiceMessage response)
			throws IOException;

}
