/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.samples.pox.ws;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.springframework.ws.server.endpoint.AbstractSaxPayloadEndpoint;

public class ContactCountEndpoint extends AbstractSaxPayloadEndpoint {

    private static final String NAMESPACE_URI = "http://www.springframework.org/spring-ws/samples/pox";

    private static final String CONTANT_NAME = "Contact";

    private static final String CONTANT_COUNT_NAME = "ContactCount";

    private DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    public ContactCountEndpoint() {
        documentBuilderFactory.setNamespaceAware(true);
    }

    protected ContentHandler createContentHandler() throws Exception {
        return new ContactCounter();
    }

    protected Source getResponse(ContentHandler contentHandler) throws Exception {
        ContactCounter counter = (ContactCounter) contentHandler;

        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document response = documentBuilder.newDocument();
        Element contactCountElement = response.createElementNS(NAMESPACE_URI, CONTANT_COUNT_NAME);
        response.appendChild(contactCountElement);
        contactCountElement.setTextContent(Integer.toString(counter.contactCount));

        return new DOMSource(response);
    }

    private static class ContactCounter extends DefaultHandler {

        private int contactCount = 0;

        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            if (NAMESPACE_URI.equals(uri) && CONTANT_NAME.equals(localName)) {
                contactCount++;
            }
        }


    }
}
