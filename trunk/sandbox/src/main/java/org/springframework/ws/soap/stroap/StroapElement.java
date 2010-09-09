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

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;

import org.springframework.util.Assert;
import org.springframework.util.xml.StaxUtils;
import org.springframework.ws.soap.SoapElement;
import org.springframework.ws.soap.SoapVersion;

/**
 * @author Arjen Poutsma
 */
abstract class StroapElement implements SoapElement {

    private final StroapMessageFactory messageFactory;

    public StroapElement(StroapMessageFactory messageFactory) {
        Assert.notNull(messageFactory, "'messageFactory' must not be null");
        this.messageFactory = messageFactory;
    }

    public final Source getSource() {
        return StaxUtils.createCustomStaxSource(getEventReader());
    }

    protected abstract XMLEventReader getEventReader();

    public void writeTo(XMLEventWriter eventWriter) throws XMLStreamException {
        eventWriter.add(getEventReader());
    }

    protected StroapMessageFactory getMessageFactory() {
        return messageFactory;
    }

    protected final XMLEventFactory getEventFactory() {
        return getMessageFactory().getEventFactory();
    }

    protected SoapVersion getSoapVersion() {
        return getMessageFactory().getSoapVersion();
    }
}
