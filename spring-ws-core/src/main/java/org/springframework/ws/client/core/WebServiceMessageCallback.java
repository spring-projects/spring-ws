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

package org.springframework.ws.client.core;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.springframework.ws.WebServiceMessage;

/**
 * Generic callback interface for code that operates on a {@link WebServiceMessage}.
 * <p>
 * Implementations can execute any number of operations on the message, such as set the contents of the message, or set
 * the {@code SOAPAction} header.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface WebServiceMessageCallback {

	/**
	 * Execute any number of operations on the supplied {@code message}.
	 *
	 * @param message the message
	 * @throws IOException in case of I/O errors
	 * @throws TransformerException in case of transformation errors
	 */
	void doWithMessage(WebServiceMessage message) throws IOException, TransformerException;

}
