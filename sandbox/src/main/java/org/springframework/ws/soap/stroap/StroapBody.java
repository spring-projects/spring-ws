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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.springframework.util.xml.StaxUtils;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.stream.StreamingPayload;

/**
 * @author Arjen Poutsma
 */
abstract class StroapBody extends StroapElement implements SoapBody {

	private StroapPayload payload;

	protected StroapBody(StroapMessageFactory messageFactory) {
		super(messageFactory.getSoapVersion().getBodyName(), messageFactory);
		this.payload = new CachingStroapPayload();
	}

	protected StroapBody(StartElement startElement, StroapPayload payload, StroapMessageFactory messageFactory) {
		super(startElement, messageFactory);
		this.payload = payload;
	}

	static StroapBody build(XMLEventReader eventReader, StroapMessageFactory messageFactory) throws XMLStreamException {
		XMLEvent event = eventReader.nextTag();
		if (!event.isStartElement()) {
			throw new StroapMessageCreationException("Unexpected event: " + event + ", expected StartElement");
		}
		StartElement startElement = event.asStartElement();
		SoapVersion soapVersion = messageFactory.getSoapVersion();
		if (!soapVersion.getBodyName().equals(startElement.getName())) {
			throw new StroapMessageCreationException(
					"Unexpected name: " + startElement.getName() + ", expected " + soapVersion.getBodyName());
		}
		StroapPayload payload;
		if (messageFactory.isPayloadCaching()) {
			payload = new CachingStroapPayload(eventReader);
		}
		else {
			payload = new NonCachingStroapPayload(eventReader);
		}

		if (SoapVersion.SOAP_11.equals(soapVersion)) {
			return new Stroap11Body(startElement, payload, messageFactory);
		}
		else {
			return null;
		}
	}

	public Source getPayloadSource() {
		XMLEventReader eventReader = payload.getEventReader();
		return StaxUtils.createCustomStaxSource(eventReader);
	}

	public Result getPayloadResult() {
		CachingStroapPayload cachingPayload;
		if (payload instanceof CachingStroapPayload) {
			cachingPayload = (CachingStroapPayload) payload;
		}
		else {
			cachingPayload = new CachingStroapPayload();
			this.payload = cachingPayload;
		}
		XMLEventWriter eventWriter = cachingPayload.getEventWriter();
		return StaxUtils.createCustomStaxResult(eventWriter);
	}

	public boolean hasFault() {
		return payload instanceof FaultStroapPayload;
	}

	public SoapFault getFault() {
		return payload instanceof FaultStroapPayload ? ((FaultStroapPayload) payload).getFault() : null;
	}

	protected void setFault(StroapFault fault) {
		this.payload = new FaultStroapPayload(fault);
	}

	@Override
	protected final XMLEventReader getChildEventReader() {
		return payload.getEventReader();
	}

	@Override
	public void writeTo(XMLEventWriter eventWriter) throws XMLStreamException {
		eventWriter.add(getStartElement());
		payload.writeTo(eventWriter);
		eventWriter.add(getEndElement());
	}

	public void setStreamingPayload(StreamingPayload payload) {
		this.payload = new StreamingStroapPayload(payload, getMessageFactory());
	}

	public QName getPayloadName() {
		return payload.getName();
	}
}
