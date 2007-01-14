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

import org.springframework.ws.context.MessageContext;

/**
 * Defines the basic contract for Web Services interested in the entire mesage payload.
 * <p/>
 * The main entrypoint is <code>invoke</code>, which gets invoked with the a message context. This context contains the
 * request, and can be used to create a response.
 *
 * @author Arjen Poutsma
 * @see #invoke(org.springframework.ws.context.MessageContext)
 */
public interface MessageEndpoint {

    /**
     * Invokes an operation. The given message context can be used to create a response.
     *
     * @param messageContext the message context
     * @throws Exception if an exception occurs
     */
    void invoke(MessageContext messageContext) throws Exception;

}
