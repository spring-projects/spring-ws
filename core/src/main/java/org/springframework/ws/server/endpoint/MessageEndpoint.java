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
 * Defines the basic contract for Web Services interested in the entire message payload.
 * <p/>
 * <p>The main entrypoint is {@link #invoke(MessageContext)}, which gets invoked with the message context. This context
 * contains the {@link MessageContext#getRequest() request}, and can be used to create a response.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.server.endpoint.PayloadEndpoint
 * @since 1.0
 */
public interface MessageEndpoint {

    /**
     * Invokes an operation.
     * <p/>
     * <p>The given <code>messageContext</code> can be used to create a response.
     *
     * @param messageContext the message context
     * @throws Exception if an exception occurs
     */
    void invoke(MessageContext messageContext) throws Exception;
}
