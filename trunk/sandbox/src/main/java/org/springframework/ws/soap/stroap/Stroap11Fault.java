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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Characters;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.soap11.Soap11Fault;
import org.springframework.xml.stream.ListBasedXMLEventReader;

/**
 * @author Arjen Poutsma
 */
class Stroap11Fault extends StroapFault implements Soap11Fault {

    private static final QName XML_LANG_NAME = new QName(XMLConstants.XML_NS_URI, "lang", XMLConstants.XML_NS_PREFIX);

    private final FaultElement faultCode;

    private final FaultElement faultString;

    private FaultElement faultActor;

    Stroap11Fault(QName faultCode, String faultString, Locale faultStringLocale, StroapMessageFactory messageFactory) {
        super(messageFactory);

        this.faultCode = FaultElement.createFaultCode(faultCode, messageFactory);
        this.faultString = FaultElement.createFaultString(faultString, faultStringLocale, messageFactory);
        addNamespaceDeclaration(faultCode.getPrefix(), faultCode.getNamespaceURI());
    }

    public QName getFaultCode() {
        return parseFaultCodeString(faultCode.getCharacterData());
    }

    private QName parseFaultCodeString(String faultCodeString) {
        if (faultCodeString == null) {
            return null;
        }
        int idx = faultCodeString.indexOf(':');
        if (idx == -1) {
            return new QName(faultCodeString);
        }
        else {
            String prefix = faultCodeString.substring(0, idx);
            String localPart = faultCodeString.substring(idx + 1, faultCodeString.length());
            String namespaceUri = getStartElement().getNamespaceURI(prefix);
            return new QName(namespaceUri, localPart, prefix);
        }
    }

    public String getFaultStringOrReason() {
        return faultString.getCharacterData();
    }

    public Locale getFaultStringLocale() {
        String xmlLangString = faultString.getAttributeValue(XML_LANG_NAME);
        if (xmlLangString != null) {
            String localeString = xmlLangString.replace('-', '_');
            return StringUtils.parseLocaleString(localeString);
        }
        return null;
    }

    public String getFaultActorOrRole() {
        return faultActor != null ? faultActor.getCharacterData() : null;
    }

    public void setFaultActorOrRole(String faultActor) {
        this.faultActor = FaultElement.createFaultActor(faultActor, getMessageFactory());
    }

    public SoapFaultDetail getFaultDetail() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public SoapFaultDetail addFaultDetail() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected List<XMLEventReader> getChildEventReaders() {
        List<XMLEventReader> eventReaders = new LinkedList<XMLEventReader>();
        eventReaders.add(faultCode.getEventReader());
        eventReaders.add(faultString.getEventReader());
        if (faultActor != null) {
            eventReaders.add(faultActor.getEventReader());
        }
        return eventReaders;
    }

    private static class FaultElement extends StroapContainer {

        private final Characters characters;

        private FaultElement(String localName, String value, StroapMessageFactory messageFactory) {
            super(messageFactory.getEventFactory().createStartElement(new QName(localName), null, null),
                    messageFactory);
            this.characters = getEventFactory().createCharacters(value);
        }

        public static FaultElement createFaultCode(QName faultCode, StroapMessageFactory messageFactory) {
            Assert.notNull(faultCode, "'faultCode' must not be null");
            Assert.hasLength(faultCode.getLocalPart(), "faultCode's localPart cannot be empty");
            Assert.hasLength(faultCode.getNamespaceURI(), "faultCode's namespaceUri cannot be empty");
            String value = faultCode.getPrefix() + ":" + faultCode.getLocalPart();
            return new FaultElement("faultcode", value, messageFactory);
        }

        public static FaultElement createFaultString(String faultString,
                                                     Locale faultStringLocale,
                                                     StroapMessageFactory messageFactory) {
            Assert.hasLength(faultString, "'faultString' must not be empty");
            FaultElement element = new FaultElement("faultstring", faultString, messageFactory);
            if (faultStringLocale != null) {
                String xmlLangString = faultStringLocale.toString().replace('_', '-');
                element.addAttribute(XML_LANG_NAME, xmlLangString);
            }
            return element;
        }

        public static FaultElement createFaultActor(String actor, StroapMessageFactory messageFactory) {
            Assert.hasLength(actor, "'actor' must not be empty");
            return new FaultElement("faultactor", actor, messageFactory);
        }

        public String getCharacterData() {
            return characters.getData();
        }

        @Override
        protected List<XMLEventReader> getChildEventReaders() {
            return Collections.<XMLEventReader>singletonList(new ListBasedXMLEventReader(characters));
        }
    }
}
