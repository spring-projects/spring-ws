/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.xml.transform;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TraxUtilsTest extends XMLTestCase {

    public void testIsStaxSourceInvalid() throws Exception {
        assertFalse("A StAX Source", TraxUtils.isStaxSource(new DOMSource()));
        assertFalse("A StAX Source", TraxUtils.isStaxSource(new SAXSource()));
        assertFalse("A StAX Source", TraxUtils.isStaxSource(new StreamSource()));
    }

    public void testIsStaxSource() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        String expected = "<element/>";
        XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader(expected));
        StaxSource source = new StaxSource(streamReader);

        assertTrue("Not a StAX Source", TraxUtils.isStaxSource(source));
    }

    public void testIsStaxSourceJaxp14() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        String expected = "<element/>";
        XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader(expected));
        StAXSource source = new StAXSource(streamReader);

        assertTrue("Not a StAX Source", TraxUtils.isStaxSource(source));
    }

    public void testIsStaxResultInvalid() throws Exception {
        assertFalse("A StAX Result", TraxUtils.isStaxResult(new DOMResult()));
        assertFalse("A StAX Result", TraxUtils.isStaxResult(new SAXResult()));
        assertFalse("A StAX Result", TraxUtils.isStaxResult(new StreamResult()));
    }

    public void testIsStaxResult() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(new StringWriter());
        StaxResult result = new StaxResult(streamWriter);

        assertTrue("Not a StAX Result", TraxUtils.isStaxResult(result));
    }

    public void testIsStaxResultJaxp14() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(new StringWriter());
        StAXResult result = new StAXResult(streamWriter);

        assertTrue("Not a StAX Result", TraxUtils.isStaxResult(result));
    }

    public void testCreateStaxSourceStreamReader() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        String expected = "<element/>";
        XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader(expected));

        Source source = TraxUtils.createStaxSource(streamReader);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringResult result = new StringResult();
        transformer.transform(source, result);

        assertXMLEqual(expected, result.toString());
    }

    public void testCreateStaxSourceEventReader() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        String expected = "<element/>";
        XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader(expected));

        Source source = TraxUtils.createStaxSource(eventReader);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringResult result = new StringResult();
        transformer.transform(source, result);

        assertXMLEqual(expected, result.toString());
    }

    public void testGetXMLStreamReader() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        String expected = "<element/>";
        XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader(expected));

        StaxSource source = new StaxSource(streamReader);

        assertEquals("Invalid XMLStreamReader", streamReader, TraxUtils.getXMLStreamReader(source));
    }

    public void testGetXMLStreamReaderJaxp14() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        String expected = "<element/>";
        XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader(expected));

        StAXSource source = new StAXSource(streamReader);

        assertEquals("Invalid XMLStreamReader", streamReader, TraxUtils.getXMLStreamReader(source));
    }

    public void testGetXMLEventReader() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        String expected = "<element/>";
        XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader(expected));

        StaxSource source = new StaxSource(eventReader);

        assertEquals("Invalid XMLEventReader", eventReader, TraxUtils.getXMLEventReader(source));
    }

    public void testGetXMLEventReaderJaxp14() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        String expected = "<element/>";
        XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader(expected));

        StAXSource source = new StAXSource(eventReader);

        assertEquals("Invalid XMLEventReader", eventReader, TraxUtils.getXMLEventReader(source));
    }

    public void testGetXMLStreamWriter() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(new StringWriter());

        StaxResult result = new StaxResult(streamWriter);

        assertEquals("Invalid XMLStreamWriter", streamWriter, TraxUtils.getXMLStreamWriter(result));
    }

    public void testGetXMLStreamWriterJaxp14() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(new StringWriter());

        StAXResult result = new StAXResult(streamWriter);

        assertEquals("Invalid XMLStreamWriter", streamWriter, TraxUtils.getXMLStreamWriter(result));
    }

    public void testGetXMLEventWriter() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(new StringWriter());

        StaxResult result = new StaxResult(eventWriter);

        assertEquals("Invalid XMLStreamWriter", eventWriter, TraxUtils.getXMLEventWriter(result));
    }

    public void testGetXMLEventWriterJaxp14() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(new StringWriter());

        StAXResult result = new StAXResult(eventWriter);

        assertEquals("Invalid XMLEventWriter", eventWriter, TraxUtils.getXMLEventWriter(result));
    }

    public void testGetDocument() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        assertSame("Invalid document", document, TraxUtils.getDocument(new DOMSource(document)));
        Element element = document.createElement("element");
        document.appendChild(element);
        assertSame("Invalid document", document, TraxUtils.getDocument(new DOMSource(element)));
    }
}