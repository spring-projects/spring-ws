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
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapHeaderException;
import org.springframework.xml.stream.ListBasedXMLEventReader;

/**
 * @author Arjen Poutsma
 */
class StroapHeaderElement extends StroapElement implements SoapHeaderElement {

    private final List<XMLEvent> events = new LinkedList<XMLEvent>();

    StroapHeaderElement(QName name, StroapMessageFactory messageFactory) {
        super(name, messageFactory);
    }

    private StroapHeaderElement(StartElement startElement, List<XMLEvent> events, StroapMessageFactory messageFactory) {
        super(startElement, messageFactory);
        Assert.notNull(events, "'events' must not be null");
        this.events.addAll(events);
    }

    static StroapHeaderElement build(List<XMLEvent> events, StroapMessageFactory messageFactory)
            throws XMLStreamException {
        Assert.notNull(events, "'events' must not be null");
        Assert.isTrue(events.size() >= 2, "not enough events");
        XMLEvent event = events.get(0);
        if (!event.isStartElement()) {
            throw new StroapHeaderException("Unexpected event: " + event + ", expected StartElement");
        }
        StartElement startElement = event.asStartElement();
        event = events.get(events.size() - 1);
        if (!event.isEndElement()) {
            throw new StroapHeaderException("Unexpected event: " + event + ", expected EndElement");
        }
        List<XMLEvent> childEvents = events.subList(1, events.size() - 1);
        return new StroapHeaderElement(startElement, childEvents, messageFactory);
    }

    public final String getActorOrRole() throws SoapHeaderException {
        return getAttributeValue(getSoapVersion().getActorOrRoleName());
    }

    public final void setActorOrRole(String actorOrRole) throws SoapHeaderException {
        addAttribute(getSoapVersion().getActorOrRoleName(), actorOrRole);
    }

    public final boolean getMustUnderstand() throws SoapHeaderException {
        String mustUnderstandAttribute = getAttributeValue(getSoapVersion().getMustUnderstandAttributeName());
        return "1".equals(mustUnderstandAttribute);
    }

    public void setMustUnderstand(boolean mustUnderstand) throws SoapHeaderException {
        String mustUnderstandAttribute = mustUnderstand ? "1" : "0";
        addAttribute(getSoapVersion().getMustUnderstandAttributeName(), mustUnderstandAttribute);
    }

    public Result getResult() throws SoapHeaderException {
        events.clear();
        return StaxUtils.createCustomStaxResult(new CachingXMLEventWriter(events));
    }

    public String getText() {
        StringBuilder builder = new StringBuilder();
        for (XMLEvent event : events) {
            if (event.isCharacters()) {
                builder.append(event.asCharacters().getData());
            }
        }
        return builder.toString();
    }

    public void setText(String content) {
        events.clear();
        events.add(getEventFactory().createCharacters(content));
    }

    @Override
    protected XMLEventReader getChildEventReader() {
        return new ListBasedXMLEventReader(events);
    }


}
