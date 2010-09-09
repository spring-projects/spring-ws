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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Result;

import org.springframework.util.Assert;
import org.springframework.util.xml.StaxUtils;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapHeaderException;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.xml.stream.AbstractXMLEventWriter;

/**
 * @author Arjen Poutsma
 */
abstract class StroapHeader extends StroapContainer implements SoapHeader {

    private List<StroapHeaderElement> headerElements = new LinkedList<StroapHeaderElement>();

    protected StroapHeader(StroapMessageFactory messageFactory) {
        super(messageFactory.getSoapVersion().getHeaderName(), messageFactory);
    }

    protected StroapHeader(StartElement startElement, StroapMessageFactory messageFactory) {
        super(startElement, messageFactory);
    }

    static StroapHeader build(XMLEventReader eventReader, StroapMessageFactory messageFactory)
            throws XMLStreamException {
        XMLEvent event = eventReader.nextTag();
        if (!event.isStartElement()) {
            throw new StroapMessageCreationException("Unexpected event: " + event + ", expected StartElement");
        }
        StartElement startElement = event.asStartElement();
        SoapVersion soapVersion = messageFactory.getSoapVersion();
        if (!soapVersion.getHeaderName().equals(startElement.getName())) {
            throw new StroapMessageCreationException(
                    "Unexpected name: " + startElement.getName() + ", expected " + soapVersion.getHeaderName());
        }

        if (SoapVersion.SOAP_11.equals(soapVersion)) {
            return new Stroap11Header(startElement, messageFactory);
        }
        else {
            return null;
        }

    }

    public SoapHeaderElement addHeaderElement(QName name) throws SoapHeaderException {
        StroapHeaderElement headerElement = new StroapHeaderElement(name, getMessageFactory());
        headerElements.add(headerElement);
        return headerElement;
    }

    public Iterator<SoapHeaderElement> examineAllHeaderElements() throws SoapHeaderException {
        List<SoapHeaderElement> headerElements = Collections.<SoapHeaderElement>unmodifiableList(this.headerElements);
        return headerElements.iterator();
    }

    public Iterator<SoapHeaderElement> examineMustUnderstandHeaderElements(String actorOrRole)
            throws SoapHeaderException {
        List<SoapHeaderElement> result = new LinkedList<SoapHeaderElement>();
        for (StroapHeaderElement headerElement : this.headerElements) {
            if (headerElement.getMustUnderstand() && headerElement.getActorOrRole().equals(actorOrRole)) {
                result.add(headerElement);
            }
        }
        return result.iterator();
    }

    public void removeHeaderElement(QName name) throws SoapHeaderException {
        Assert.notNull(name, "'name' must not be null");

        for (Iterator<StroapHeaderElement> iterator = headerElements.iterator(); iterator.hasNext();) {
            StroapHeaderElement headerElement = iterator.next();
            if (name.equals(headerElement.getName())) {
                iterator.remove();
                break;
            }
        }
    }

    @Override
    protected List<XMLEventReader> getChildEventReaders() {
        List<XMLEventReader> result = new LinkedList<XMLEventReader>();
        for (StroapHeaderElement headerElement : headerElements) {
            result.add(headerElement.getEventReader());
        }
        return result;
    }

    public Result getResult() {
        headerElements.clear();
        return StaxUtils.createCustomStaxResult(new StroapHeaderXMLEventWriter());
    }

    class StroapHeaderXMLEventWriter extends AbstractXMLEventWriter {

        private int elementDepth = 0;

        boolean startElementSeen = false;

        private final List<XMLEvent> events = new LinkedList<XMLEvent>();

        public void add(XMLEvent event) throws XMLStreamException {
            if (event.isStartElement()) {
                startElementSeen = true;
                elementDepth++;
            }
            else if (event.isEndElement()) {
                elementDepth--;
            }
            else if (event.isStartDocument() || event.isEndDocument()) {
                return;
            }
            if (elementDepth >= 0 && startElementSeen) {
                events.add(event);
            }
            if (elementDepth == 0 && (event.isEndElement() || event.isEndDocument())) {
                StroapHeaderElement headerElement = StroapHeaderElement.build(events, getMessageFactory());
                headerElements.add(headerElement);
                events.clear();
            }
        }
    }

}
