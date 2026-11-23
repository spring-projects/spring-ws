/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport;

import java.io.IOException;
import java.util.Iterator;

/**
 * Sub-interface of {@link WebServiceConnection} that is aware of response headers and can
 * define request headers.
 *
 * @author Greg Turnquist
 * @since 2.3
 */
public interface HeadersAwareSenderWebServiceConnection extends WebServiceConnection {

	/**
	 * Return an iteration over all the header names the response contains. Returns an
	 * empty {@code Iterator} if there are no headers.
	 */
	Iterator<String> getResponseHeaderNames() throws IOException;

	/**
	 * Return an iteration over all the string values of the specified response header.
	 * Return an empty {@code Iterator} if there is no header of the specified name.
	 */
	Iterator<String> getResponseHeaders(String name) throws IOException;

	/**
	 * Adds a request header with the given name and value. This method can be called
	 * multiple times, to allow for headers with multiple values.
	 * @param name the name of the header
	 * @param value the value of the header
	 */
	void addRequestHeader(String name, String value) throws IOException;

}
