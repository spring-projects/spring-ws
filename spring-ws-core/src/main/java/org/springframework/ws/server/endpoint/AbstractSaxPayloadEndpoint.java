/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ws.server.endpoint;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXResult;

import org.springframework.xml.transform.TransformerObjectSupport;
import org.xml.sax.ContentHandler;

/**
 * Abstract base class for endpoints that handle the message payload with a SAX {@code ContentHandler}. Allows
 * subclasses to create a response by returning a {@code Source}.
 * <p>
 * Implementations of this class should create a new handler for each call of {@code createContentHandler}, because of
 * thread safety. The handlers is later passed on to {@code createResponse}, so it can be used for holding
 * request-specific state.
 *
 * @author Arjen Poutsma
 * @see #createContentHandler()
 * @see #getResponse(org.xml.sax.ContentHandler)
 * @since 1.0.0
 * @deprecated as of Spring Web Services 2.0, in favor of annotated endpoints
 */
@Deprecated
public abstract class AbstractSaxPayloadEndpoint extends TransformerObjectSupport implements PayloadEndpoint {

	/**
	 * Invokes the provided {@code ContentHandler} on the given request. After parsing has been done, the provided
	 * response is returned.
	 *
	 * @see #createContentHandler()
	 * @see #getResponse(org.xml.sax.ContentHandler)
	 */
	@Override
	public final Source invoke(Source request) throws Exception {
		ContentHandler contentHandler = null;
		if (request != null) {
			contentHandler = createContentHandler();
			SAXResult result = new SAXResult(contentHandler);
			transform(request, result);
		}
		return getResponse(contentHandler);
	}

	/**
	 * Returns the SAX {@code ContentHandler} used to parse the incoming request payload. A new instance should be created
	 * for each call, because of thread-safety. The content handler can be used to hold request-specific state.
	 * <p>
	 * If an incoming message does not contain a payload, this method will not be invoked.
	 *
	 * @return a SAX content handler to be used for parsing
	 */
	protected abstract ContentHandler createContentHandler() throws Exception;

	/**
	 * Returns the response to be given, if any. This method is called after the request payload has been parsed using the
	 * SAX {@code ContentHandler}. The passed {@code ContentHandler} is created by {@link #createContentHandler()}: it can
	 * be used to hold request-specific state.
	 * <p>
	 * If an incoming message does not contain a payload, this method will be invoked with {@code null} as content
	 * handler.
	 *
	 * @param contentHandler the content handler used to parse the request
	 */
	protected abstract Source getResponse(ContentHandler contentHandler) throws Exception;
}
