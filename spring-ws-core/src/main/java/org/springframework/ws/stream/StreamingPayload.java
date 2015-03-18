/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.stream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Defines the contract for payloads that can be written directly to a {@link XMLStreamWriter}.
 *
 * @author Arjen Poutsma
 * @see StreamingWebServiceMessage
 * @since 2.0
 */
public interface StreamingPayload {

	/**
	 * Returns the qualified name of the payload.
	 *
	 * @return the qualified name
	 */
	QName getName();

	/**
	 * Writes this payload to the given {@link XMLStreamWriter}.
	 *
	 * @param streamWriter the stream writer to write to
	 * @throws XMLStreamException in case of errors
	 */
	void writeTo(XMLStreamWriter streamWriter) throws XMLStreamException;

}
