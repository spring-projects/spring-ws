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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.xml.stream.CompositeXMLEventReader;
import org.springframework.xml.stream.ListBasedXMLEventReader;

/**
 * @author Arjen Poutsma
 */
abstract class StroapContainer extends StroapElement {

    static final String PREFIX = "SOAP-ENV";

    private StartElement startElement;

    private EndElement endElement;

    protected StroapContainer(QName name, StroapMessageFactory messageFactory) {
        super(messageFactory);
        Assert.notNull(name, "'name' must not be null");
        if (!StringUtils.hasLength(name.getPrefix())) {
            name = new QName(name.getNamespaceURI(), name.getLocalPart(), PREFIX);
        }
        this.startElement = getEventFactory().createStartElement(name, null, null);
        this.endElement = getEventFactory().createEndElement(name, null);
    }

    protected StroapContainer(StartElement startElement, StroapMessageFactory messageFactory) {
        super(messageFactory);
        Assert.notNull(startElement, "'startElement' must not be null");
        this.startElement = startElement;
        this.endElement = getEventFactory().createEndElement(startElement.getName(), startElement.getNamespaces());
    }

    public final QName getName() {
        return getStartElement().getName();
    }

    @Override
    protected XMLEventReader getEventReader() {
        List<XMLEventReader> eventReaders = new LinkedList<XMLEventReader>();
        eventReaders.add(new ListBasedXMLEventReader(startElement));
        eventReaders.addAll(getChildEventReaders());
        eventReaders.add(new ListBasedXMLEventReader(endElement));

        return new CompositeXMLEventReader(eventReaders);
    }

    protected abstract List<XMLEventReader> getChildEventReaders();

    // Attributes

    public final Iterator<QName> getAllAttributes() {
        List<QName> result = new LinkedList<QName>();
        for (Iterator iterator = getStartElement().getAttributes(); iterator.hasNext();) {
            Attribute attribute = (Attribute) iterator.next();
            result.add(attribute.getName());
        }
        return result.iterator();
    }

    public final String getAttributeValue(QName name) {
        Attribute attribute = getStartElement().getAttributeByName(name);
        return attribute != null ? attribute.getValue() : null;
    }

    public final void removeAttribute(QName name) {
        List<Attribute> newAttributes = new LinkedList<Attribute>();
        for (Iterator iterator = getStartElement().getAttributes(); iterator.hasNext();) {
            Attribute attribute = (Attribute) iterator.next();
            if (!name.equals(attribute.getName())) {
                newAttributes.add(attribute);
            }
        }
        StartElement oldStartElement = getStartElement();
        this.startElement = getEventFactory().createStartElement(oldStartElement.getName(), newAttributes.iterator(),
                oldStartElement.getNamespaces());
    }

    public final void addAttribute(QName name, String value) {
        List<Attribute> newAttributes = new LinkedList<Attribute>();
        for (Iterator iterator = getStartElement().getAttributes(); iterator.hasNext();) {
            Attribute attribute = (Attribute) iterator.next();
            newAttributes.add(attribute);
        }
        Attribute newAttribute = getEventFactory().createAttribute(name, value);
        newAttributes.add(newAttribute);
        StartElement oldStartElement = getStartElement();
        this.startElement = getEventFactory().createStartElement(oldStartElement.getName(), newAttributes.iterator(),
                oldStartElement.getNamespaces());
    }

    // Namespaces

    public final void addNamespaceDeclaration(String prefix, String namespaceUri) {
        List<Namespace> newNamespaces = new LinkedList<Namespace>();
        for (Iterator iterator = getStartElement().getNamespaces(); iterator.hasNext();) {
            Namespace namespace = (Namespace) iterator.next();
            newNamespaces.add(namespace);
        }
        Namespace newNamespace;
        if (StringUtils.hasLength(prefix)) {
            newNamespace = getEventFactory().createNamespace(prefix, namespaceUri);
        }
        else {
            newNamespace = getEventFactory().createNamespace(namespaceUri);
        }
        newNamespaces.add(newNamespace);
        StartElement oldStartElement = getStartElement();
        this.startElement = getEventFactory()
                .createStartElement(oldStartElement.getName(), oldStartElement.getAttributes(),
                        newNamespaces.iterator());
    }

    protected final StartElement getStartElement() {
        return startElement;
    }

    protected final EndElement getEndElement() {
        return endElement;
    }

}
