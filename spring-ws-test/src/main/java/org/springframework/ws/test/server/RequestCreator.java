/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ws.test.server;

import java.io.IOException;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

/**
 * Creates request messages. Implementations of this interface are returned by {@link RequestCreators}.
 *
 * @author Arjen Poutsma
 * @see RequestCreators
 * @since 2.0
 */
public interface RequestCreator {

	/**
	 * Create a request.
	 *
	 * @param messageFactory the message that can be used to create responses
	 * @throws IOException in case of I/O errors
	 */
	WebServiceMessage createRequest(WebServiceMessageFactory messageFactory) throws IOException;

}
