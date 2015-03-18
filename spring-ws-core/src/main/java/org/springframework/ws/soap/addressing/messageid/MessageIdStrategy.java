/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.ws.soap.addressing.messageid;

import java.net.URI;

import org.springframework.ws.soap.SoapMessage;

/**
 * Strategy interface that encapsulates the creation and validation of WS-Addressing {@code MessageID}s.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public interface MessageIdStrategy {

	/**
	 * Indicates whether the given {@code MessageID} value is a duplicate or not
	 *
	 * @param messageId the message id
	 * @return {@code true} if a duplicate; {@code false} otherwise
	 */
	boolean isDuplicate(URI messageId);

	/**
	 * Returns a new WS-Addressing {@code MessageID} for the given {@link SoapMessage}.
	 *
	 * @param message the message to create an id for
	 * @return the new message id
	 */
	URI newMessageId(SoapMessage message);

}
