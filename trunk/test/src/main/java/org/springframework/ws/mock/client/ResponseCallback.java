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

package org.springframework.ws.mock.client;

import java.io.IOException;
import java.net.URI;

import org.springframework.ws.WebServiceMessage;

/**
 * Callback interface for code that operates on response {@link org.springframework.ws.WebServiceMessage}s. Defines the
 * contract for creating responses in test scenarios.
 *
 * @author Arjen Poutsma
 * @author Lukas Krecan
 * @since 2.0
 */
public interface ResponseCallback {

    /**
     * Execute any number of operations on the supplied response, given the request.
     *
     * @param uri      of the service called
     * @param request  the request message
     * @param response the response message
     * @throws IOException in case of I/O errors
     */
    void doWithResponse(URI uri, WebServiceMessage request, WebServiceMessage response) throws IOException;

}