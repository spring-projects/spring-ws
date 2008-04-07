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

package org.springframework.ws.server;

import org.springframework.ws.context.MessageContext;

/**
 * Defines the interface for objects than can resolve exceptions thrown during endpoint execution, resulting in SOAP
 * messages.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface EndpointExceptionResolver {

    /**
     * Try to resolve the given exception that got thrown during on endpoint execution.
     *
     * @param messageContext current message context
     * @param endpoint       the executed endpoint, or null if none chosen at the time of the exception
     * @param ex             the exception that got thrown during endpoint execution
     * @return <code>true</code> if resolved; <code>false</code> otherwise
     */
    boolean resolveException(MessageContext messageContext, Object endpoint, Exception ex);
}
