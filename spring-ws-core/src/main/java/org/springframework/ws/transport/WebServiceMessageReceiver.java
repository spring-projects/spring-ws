/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.ws.transport;

import org.springframework.ws.context.MessageContext;

/**
 * Defines the methods for classes capable of receiving {@link org.springframework.ws.WebServiceMessage} instances
 * coming in on a transport.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface WebServiceMessageReceiver {

	/**
	 * Receives the given message context. The given message context can be used to create a response.
	 *
	 * @param messageContext the message context to be received
	 */
	void receive(MessageContext messageContext) throws Exception;

}
