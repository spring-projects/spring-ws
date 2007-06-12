/*
 * Copyright 2005 the original author or authors.
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
 * Abstract base class for endpoints that handle the message payload with a SAX <code>ContentHandler</code>. Allows
 * subclasses to create a response by returning a <code>Source</code>.
 * <p/>
 * Implementations of this class should create a new handler for each call of <code>createContentHandler</code>, because
 * of thread safety. The handlers is later passed on to <code>createResponse</code>, so it can be used for holding
 * request-specific state.
 *
 * @author Arjen Poutsma
 * @see #createContentHandler()
 * @see #getResponse(org.xml.sax.ContentHandler)
 */
public abstract class AbstractSaxPayloadEndpoint extends TransformerObjectSupport implements PayloadEndpoint {

    /**
     * Invokes the provided <code>ContentHandler</code> and <code>LexicalHandler</code> on the given request. After
     * parsing has been done, the provided response is returned.
     *
     * @see #createContentHandler()
     * @see #getResponse(org.xml.sax.ContentHandler)
     */
    public final Source invoke(Source request) throws Exception {
        ContentHandler contentHandler = createContentHandler();
        SAXResult result = new SAXResult(contentHandler);
        transform(request, result);
        return getResponse(contentHandler);
    }

    /**
     * Returns the SAX <code>ContentHandler</code> used to parse the incoming request payload. A new instance should be
     * created for each call, because of thread-safety. The content handler can be used to hold request-specific state.
     *
     * @return a SAX content handler to be used for parsing
     */
    protected abstract ContentHandler createContentHandler() throws Exception;

    /**
     * Returns the response to be given, if any. This method is called after the request payload has been parse using
     * the SAX <code>ContentHandler</code>. The passed <code>ContentHandler</code> is created by
     * <code>createContentHandler</code>: it can be used to hold request-specific state.
     *
     * @param contentHandler the content handler used to parse the request
     */
    protected abstract Source getResponse(ContentHandler contentHandler) throws Exception;
}
