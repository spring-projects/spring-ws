/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server;

import org.springframework.ws.context.MessageContext;

/**
 * Extension of the {@link EndpointInterceptor} interface that adds a way to
 * @author Arjen Poutsma
 * @since 2.0
 */
public interface SmartEndpointInterceptor extends EndpointInterceptor {

    /**
     * Indicates whether this interceptor should intercept the given message context.
     *
     * @param messageContext contains the incoming request message
     * @param endpoint       chosen endpoint to invoke
     * @return {@code true} to indicate that this interceptor applies; {@code false} otherwise
     */
    boolean shouldIntercept(MessageContext messageContext, Object endpoint);

}
