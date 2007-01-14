/*
 * Copyright 2005 the original author or authors.
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

package org.springframework.ws.server.endpoint;

import javax.xml.transform.Source;

import org.springframework.xml.transform.StringSource;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SaxPayloadEndpointTest extends AbstractPayloadEndpointTestCase {

    protected PayloadEndpoint createNoResponseEndpoint() throws Exception {
        return new AbstractSaxPayloadEndpoint() {

            protected Source getResponse(ContentHandler contentHandler) {
                return null;
            }

            protected ContentHandler createContentHandler() {
                return new DefaultHandler();
            }
        };
    }

    protected PayloadEndpoint createResponseEndpoint() throws Exception {
        return new AbstractSaxPayloadEndpoint() {

            protected ContentHandler createContentHandler() {
                return new TestContentHandler();
            }

            protected Source getResponse(ContentHandler contentHandler) {
                return new StringSource(RESPONSE);
            }
        };
    }

    private static class TestContentHandler extends DefaultHandler {

        public void endElement(String uri, String localName, String qName) throws SAXException {
            assertEquals("Invalid local name", REQUEST_ELEMENT, localName);
            assertEquals("Invalid qName", REQUEST_ELEMENT, localName);
            assertEquals("Invalid namespace", NAMESPACE_URI, uri);
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            assertEquals("Invalid local name", REQUEST_ELEMENT, localName);
            assertEquals("Invalid qName", REQUEST_ELEMENT, localName);
            assertEquals("Invalid namespace", NAMESPACE_URI, uri);
        }
    }
}
