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

package org.springframework.ws.soap.stroap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.springframework.util.Assert;
import org.springframework.util.xml.StaxUtils;
import org.springframework.ws.stream.StreamingPayload;

/**
 * @author Arjen Poutsma
 */
class StreamingStroapPayload extends StroapPayload {

	private final StreamingPayload payload;

	private final StroapMessageFactory messageFactory;

	StreamingStroapPayload(StreamingPayload payload, StroapMessageFactory messageFactory) {
		Assert.notNull(payload, "'payload' must not be null");
		Assert.notNull(messageFactory, "'messageFactory' must not be null");

		this.payload = payload;
		this.messageFactory = messageFactory;
	}

	@Override
	public QName getName() {
		return payload.getName();
	}

	@Override
	public XMLEventReader getEventReader() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			XMLStreamWriter streamWriter = messageFactory.getOutputFactory().createXMLStreamWriter(bos);
			payload.writeTo(streamWriter);
			streamWriter.flush();
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			return messageFactory.getInputFactory().createXMLEventReader(bis);
		}
		catch (XMLStreamException ex) {
			throw new StroapBodyException(ex);
		}
	}

	@Override
	public void writeTo(XMLEventWriter eventWriter) throws XMLStreamException {
		XMLStreamWriter streamWriter = StaxUtils.createEventStreamWriter(eventWriter, messageFactory.getEventFactory());
		payload.writeTo(streamWriter);
	}

}
