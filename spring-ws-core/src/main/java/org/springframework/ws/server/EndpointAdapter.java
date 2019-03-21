/*
 * Copyright 2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
 * Interface that must be implemented for each endpoint type to handle a message request. This interface is used to
 * allow the {@code MessageDispatcher} to be indefinitely extensible. It accesses all installed endpoints through
 * this interface, meaning that is does not contain code specific to any endpoint type.
 *
 * <p>This interface is not intended for application developers. It is available for those who want to develop their own
 * message flow.
 *
 * @author Arjen Poutsma
 * @see MessageDispatcher
 * @since 1.0.0
 */
public interface EndpointAdapter {

	/**
	 * Does this {@code EndpointAdapter} support the given {@code endpoint}?
	 *
	 * <p>Typical {@code EndpointAdapters} will base the decision on the endpoint type.
	 *
	 * @param endpoint endpoint object to check
	 * @return {@code true} if this {@code EndpointAdapter} supports the supplied {@code endpoint}
	 */
	boolean supports(Object endpoint);

	/**
	 * Use the given {@code endpoint} to handle the request.
	 *
	 * @param messageContext the current message context
	 * @param endpoint		 the endpoint to use. This object must have previously been passed to the {@link
	 *						 #supports(Object)} method of this interface, which must have returned {@code true}
	 * @throws Exception in case of errors
	 */
	void invoke(MessageContext messageContext, Object endpoint) throws Exception;
}
