/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.stream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Defines the contract for payloads that can be written directly to a
 * {@link XMLStreamWriter}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 * @see StreamingWebServiceMessage
 */
public interface StreamingPayload {

	/**
	 * Returns the qualified name of the payload. Only the namespace URI and local part of
	 * the returned qualified name are significant; they must match the name of the root
	 * element produced by {@link #writeTo(XMLStreamWriter)}.
	 * @return the qualified name
	 */
	QName getName();

	/**
	 * Writes this payload to the given {@link XMLStreamWriter}.
	 * @param streamWriter the stream writer to write to
	 * @throws XMLStreamException in case of errors
	 */
	void writeTo(XMLStreamWriter streamWriter) throws XMLStreamException;

}
