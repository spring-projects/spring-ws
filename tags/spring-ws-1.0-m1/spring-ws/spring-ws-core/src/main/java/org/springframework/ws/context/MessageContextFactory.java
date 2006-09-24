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

package org.springframework.ws.context;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

/**
 * The <code>MessageContextFactory</code> serves as factory for <code>MessageContext</code>s. Allows creation of
 * contexts based on HTTP requests, and other transports.
 *
 * @author Arjen Poutsma
 */
public interface MessageContextFactory {

    /**
     * Creates a <code>MessaageContext</code> based on the given HTTP request. Implementations use the requests input
     * stream to create a request message, and possibly copy the request headers to the message.
     *
     * @param request the HTTP request
     * @return the created message context
     * @throws IOException if an I/O exception occurs
     * @see HttpServletRequest#getInputStream()
     * @see HttpServletRequest#getHeaders(String)
     */
    MessageContext createContext(HttpServletRequest request) throws IOException;

}
