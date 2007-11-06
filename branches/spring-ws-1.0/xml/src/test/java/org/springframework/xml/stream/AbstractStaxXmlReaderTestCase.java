/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.xml.stream;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;
import org.easymock.AbstractMatcher;
import org.easymock.MockControl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public abstract class AbstractStaxXmlReaderTestCase extends TestCase {

    protected static XMLInputFactory inputFactory = XMLInputFactory.newInstance();

    private static final String XML_DTD_HANDLER =
            "<!DOCTYPE beans PUBLIC '-//SPRING//DTD BEAN//EN' 'http://www.springframework.org/dtd/spring-beans.dtd'><beans />";

    private static final String XML_CONTENT_HANDLER =
            "<?pi content?><root xmlns='namespace'><prefix:child xmlns:prefix='namespace2'>content</prefix:child></root>";

    private static final String XML_CONTENT_HANDLER_ATTS = "<element xmlns='namespace' attr='value'/>";

    private XMLReader reader;

    protected void setUp() throws Exception {
        reader = XMLReaderFactory.createXMLReader();
        reader.setFeature("http://xml.org/sax/features/namespaces", true);
        reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
    }

    public void testContentHandler() throws SAXException, IOException, XMLStreamException {
        // record the callbacks by parsing the XML with a regular SAX parser
        MockControl control = MockControl.createStrictControl(ContentHandler.class);
        control.setDefaultMatcher(new SaxArgumentMatcher());
        ContentHandler mock = (ContentHandler) control.getMock();
        reader.setContentHandler(mock);
        reader.parse(new InputSource(new StringReader(XML_CONTENT_HANDLER)));
        control.replay();
        AbstractStaxXmlReader staxXmlReader = createStaxXmlReader(new StringReader(XML_CONTENT_HANDLER));
        staxXmlReader.setContentHandler(mock);
        staxXmlReader.parse(new InputSource());
        control.verify();
    }

    public void testContentHandlerAttributes() throws SAXException, IOException, XMLStreamException {
        MockControl control = MockControl.createStrictControl(ContentHandler.class);
        control.setDefaultMatcher(new SaxArgumentMatcher());
        ContentHandler mock = (ContentHandler) control.getMock();
        reader.setContentHandler(mock);
        reader.parse(new InputSource(new StringReader(XML_CONTENT_HANDLER_ATTS)));
        control.replay();
        AbstractStaxXmlReader staxXmlReader = createStaxXmlReader(new StringReader(XML_CONTENT_HANDLER_ATTS));
        staxXmlReader.setContentHandler(mock);
        staxXmlReader.parse(new InputSource());
        control.verify();
    }

    public void testDtdHandler() throws IOException, SAXException, XMLStreamException {
        // record the callbacks by parsing the XML with a regular SAX parser
        MockControl control = MockControl.createStrictControl(DTDHandler.class);
        control.setDefaultMatcher(new SaxArgumentMatcher());
        DTDHandler mock = (DTDHandler) control.getMock();
        reader.setDTDHandler(mock);
        reader.parse(new InputSource(new StringReader(XML_DTD_HANDLER)));
        control.replay();
        AbstractStaxXmlReader staxXmlReader = createStaxXmlReader(new StringReader(XML_DTD_HANDLER));
        staxXmlReader.setDTDHandler(mock);
        staxXmlReader.parse(new InputSource());
        control.verify();
    }

    protected abstract AbstractStaxXmlReader createStaxXmlReader(Reader reader) throws XMLStreamException;

    /** Easymock <code>ArgumentMatcher</code> implementation that matches SAX arguments. */
    protected static class SaxArgumentMatcher extends AbstractMatcher {

        public boolean matches(Object[] expected, Object[] actual) {
            if (expected == actual) {
                return true;
            }
            if (expected == null || actual == null) {
                return false;
            }
            if (expected.length != actual.length) {
                throw new IllegalArgumentException("Expected and actual arguments must have the same size");
            }
            if (expected.length == 3 && expected[0] instanceof char[] && expected[1] instanceof Integer &&
                    expected[2] instanceof Integer) {
                // handling of the character(char[], int, int) methods
                String expectedString = new String((char[]) expected[0], ((Integer) expected[1]).intValue(),
                        ((Integer) expected[2]).intValue());
                String actualString = new String((char[]) actual[0], ((Integer) actual[1]).intValue(),
                        ((Integer) actual[2]).intValue());
                return expectedString.equals(actualString);
            }
            else if (expected.length == 1 && (expected[0] instanceof Locator)) {
                return true;
            }
            else {
                return super.matches(expected, actual);
            }
        }

        protected boolean argumentMatches(Object expected, Object actual) {
            if (expected instanceof char[]) {
                return Arrays.equals((char[]) expected, (char[]) actual);
            }
            else if (expected instanceof Attributes) {
                Attributes expectedAttributes = (Attributes) expected;
                Attributes actualAttributes = (Attributes) actual;
                if (expectedAttributes.getLength() != actualAttributes.getLength()) {
                    return false;
                }
                for (int i = 0; i < expectedAttributes.getLength(); i++) {
                    if (!expectedAttributes.getURI(i).equals(actualAttributes.getURI(i)) ||
                            !expectedAttributes.getQName(i).equals(actualAttributes.getQName(i)) ||
                            !expectedAttributes.getType(i).equals(actualAttributes.getType(i)) ||
                            !expectedAttributes.getValue(i).equals(actualAttributes.getValue(i))) {
                        return false;
                    }
                }
                return true;
            }
            else if (expected instanceof Locator) {
                Locator expectedLocator = (Locator) expected;
                Locator actualLocator = (Locator) actual;
                return expectedLocator.getColumnNumber() == actualLocator.getColumnNumber() &&
                        expectedLocator.getLineNumber() == actualLocator.getLineNumber();
            }
            return super.argumentMatches(expected, actual);
        }

        protected String argumentToString(Object argument) {
            if (argument instanceof char[]) {
                char[] array = (char[]) argument;
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < array.length; i++) {
                    buffer.append(array[i]);
                }
                return buffer.toString();
            }
            else if (argument instanceof Attributes) {
                Attributes attributes = (Attributes) argument;
                StringBuffer buffer = new StringBuffer("[");
                for (int i = 0; i < attributes.getLength(); i++) {
                    buffer.append('{');
                    buffer.append(attributes.getURI(i));
                    buffer.append('}');
                    buffer.append(attributes.getQName(i));
                    buffer.append('=');
                    buffer.append(attributes.getValue(i));
                    if (i < attributes.getLength() - 1) {
                        buffer.append(", ");
                    }
                }
                buffer.append(']');
                return buffer.toString();
            }
            else if (argument instanceof Locator) {
                Locator locator = (Locator) argument;
                StringBuffer buffer = new StringBuffer("[");
                buffer.append(locator.getLineNumber());
                buffer.append(',');
                buffer.append(locator.getColumnNumber());
                buffer.append(']');
                return buffer.toString();
            }
            else {
                return super.argumentToString(argument);
            }
        }
    }


}
