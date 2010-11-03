/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.test.client;

import java.io.IOException;
import java.net.URI;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

/**
 * Abstract base class for the {@link ResponseCreator} interface.
 * <p/>
 * Creates a response using the given {@link WebServiceMessageFactory}, and passes it on to {@link #doWithResponse(URI,
 * WebServiceMessage, WebServiceMessage)}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
abstract class AbstractResponseCreator implements ResponseCreator {

    public final WebServiceMessage createResponse(URI uri,
                                                  WebServiceMessage request,
                                                  WebServiceMessageFactory messageFactory) throws IOException {
        WebServiceMessage response = messageFactory.createWebServiceMessage();
        doWithResponse(uri, request, response);
        return response;
    }

    /**
     * Execute any number of operations on the supplied response, given the request and URI.
     *
     * @param uri      the URI
     * @param request  the request message
     * @param response the response message
     * @throws IOException in case of I/O errors
     */
    protected abstract void doWithResponse(URI uri, WebServiceMessage request, WebServiceMessage response)
            throws IOException;

}
