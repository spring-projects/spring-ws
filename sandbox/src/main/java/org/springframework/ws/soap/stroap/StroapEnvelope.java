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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapBodyException;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderException;
import org.springframework.ws.soap.SoapVersion;

/**
 * @author Arjen Poutsma
 */
class StroapEnvelope extends StroapContainer implements SoapEnvelope {

    private static final String LOCAL_NAME = "Envelope";

    private StroapHeader header;

    private StroapBody body;

    StroapEnvelope(StroapMessageFactory messageFactory) {
        super(messageFactory.getSoapVersion().getEnvelopeName(), messageFactory);
        this.header = null;
        this.body = new Stroap11Body(messageFactory);
    }

    private StroapEnvelope(StartElement startElement,
                           StroapHeader header,
                           StroapBody body,
                           StroapMessageFactory messageFactory) {
        super(startElement, messageFactory);
        this.header = header;
        this.body = body;
    }

    static StroapEnvelope build(XMLEventReader eventReader, StroapMessageFactory messageFactory)
            throws XMLStreamException {
        XMLEvent event = eventReader.nextTag();
        if (!event.isStartElement()) {
            throw new StroapMessageCreationException("Unexpected event: " + event + ", expected StartElement");
        }
        StartElement startElement = event.asStartElement();
        SoapVersion soapVersion = messageFactory.getSoapVersion();
        if (!soapVersion.getEnvelopeName().equals(startElement.getName())) {
            throw new StroapMessageCreationException(
                    "Unexpected name: " + startElement.getName() + ", expected " + soapVersion.getEnvelopeName());
        }
        StroapHeader header = null;
        StroapBody body = null;
        XMLEvent peekedEvent = eventReader.peek();
        while (peekedEvent != null) {
            if (peekedEvent.isStartElement()) {
                QName headerOrBodyName = peekedEvent.asStartElement().getName();
                if (soapVersion.getHeaderName().equals(headerOrBodyName)) {
                    header = StroapHeader.build(eventReader, messageFactory);
                }
                else if (soapVersion.getBodyName().equals(headerOrBodyName)) {
                    body = StroapBody.build(eventReader, messageFactory);
                    break;
                }
                else {
                    throw new StroapMessageCreationException(
                            "Unexpected start element name [" + headerOrBodyName + "]");
                }
            }
            else {
                eventReader.nextEvent();
            }
            peekedEvent = eventReader.peek();
        }
        if (body == null) {
            throw new StroapMessageCreationException("No SOAP body found");
        }

        return new StroapEnvelope(startElement, header, body, messageFactory);
    }

    public SoapHeader getHeader() throws SoapHeaderException {
        if (header == null) {
            header = new Stroap11Header(getMessageFactory());
        }
        return header;
    }

    public SoapBody getBody() throws SoapBodyException {
        if (body == null) {
            body = new Stroap11Body(getMessageFactory());
        }
        return body;
    }

    @Override
    protected XMLEventReader[] getChildEventReaders() {
        if (header != null) {
            return new XMLEventReader[]{header.getEventReader(), body.getEventReader()};
        }
        else {
            return new XMLEventReader[]{body.getEventReader()};
        }
    }

}
