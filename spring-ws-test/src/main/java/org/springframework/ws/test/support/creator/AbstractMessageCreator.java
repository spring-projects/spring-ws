/*
 * Copyright 2005-present the original author or authors.
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

package org.springframework.ws.test.support.creator;

import java.io.IOException;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

/**
 * Abstract base class for the {@link WebServiceMessageCreator} interface.
 * <p>
 * Creates a message using the given {@link WebServiceMessageFactory}, and passes it on to
 * {@link #doWithMessage(WebServiceMessage)}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public abstract class AbstractMessageCreator implements WebServiceMessageCreator {

	@Override
	public final WebServiceMessage createMessage(WebServiceMessageFactory messageFactory) throws IOException {
		WebServiceMessage message = messageFactory.createWebServiceMessage();
		doWithMessage(message);
		return message;
	}

	/**
	 * Abstract template method, invoked by
	 * {@link #createMessage(WebServiceMessageFactory)} after a message has been created.
	 * @param message the message
	 * @throws IOException in case of I/O errors
	 */
	protected abstract void doWithMessage(WebServiceMessage message) throws IOException;

}
