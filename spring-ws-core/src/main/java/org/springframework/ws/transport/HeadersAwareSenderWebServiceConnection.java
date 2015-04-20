/*
 * Copyright 2005-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ws.transport;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author Greg Turnquist
 * @since 2.3
 */
public interface HeadersAwareSenderWebServiceConnection {

	/**
	 * Returns an iteration over all the header names this request contains. Returns an empty {@code Iterator} if
	 * there are no headers.
	 */
	Iterator<String> getResponseHeaderNames() throws IOException;

	/**
	 * Returns an iteration over all the string values of the specified header. Returns an empty {@code Iterator}
	 * if there are no headers of the specified name.
	 */
	Iterator<String> getResponseHeaders(String name) throws IOException;

	/**
	 * Adds a request header with the given name and value. This method can be called multiple times, to allow for
	 * headers with multiple values.
	 *
	 * @param name	the name of the header
	 * @param value the value of the header
	 */
	void addRequestHeader(String name, String value) throws IOException;

}
