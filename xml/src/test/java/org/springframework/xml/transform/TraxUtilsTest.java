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

package org.springframework.xml.transform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
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
import org.easymock.MockControl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

@SuppressWarnings("Since15")
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

    public void testDoWithDomSource() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        MockControl control = MockControl.createControl(TraxUtils.SourceCallback.class);
        TraxUtils.SourceCallback mock = (TraxUtils.SourceCallback) control.getMock();
        mock.domSource(document);
        control.replay();

        TraxUtils.doWithSource(new DOMSource(document), mock);

        control.verify();
    }

    public void testDoWithDomResult() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        MockControl control = MockControl.createControl(TraxUtils.ResultCallback.class);
        TraxUtils.ResultCallback mock = (TraxUtils.ResultCallback) control.getMock();
        mock.domResult(document);
        control.replay();

        TraxUtils.doWithResult(new DOMResult(document), mock);

        control.verify();
    }

    public void testDoWithSaxSource() throws Exception {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        InputSource inputSource = new InputSource();

        MockControl control = MockControl.createControl(TraxUtils.SourceCallback.class);
        TraxUtils.SourceCallback mock = (TraxUtils.SourceCallback) control.getMock();
        mock.saxSource(reader, inputSource);
        control.replay();

        TraxUtils.doWithSource(new SAXSource(reader, inputSource), mock);

        control.verify();
    }

    public void testDoWithSaxResult() throws Exception {
        ContentHandler contentHandler = new DefaultHandler();
        LexicalHandler lexicalHandler = new DefaultHandler2();

        MockControl control = MockControl.createControl(TraxUtils.ResultCallback.class);
        TraxUtils.ResultCallback mock = (TraxUtils.ResultCallback) control.getMock();
        mock.saxResult(contentHandler, lexicalHandler);
        control.replay();

        SAXResult result = new SAXResult(contentHandler);
        result.setLexicalHandler(lexicalHandler);
        TraxUtils.doWithResult(result, mock);

        control.verify();
    }

    public void testDoWithStaxSourceEventReader() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader("<element/>"));

        MockControl control = MockControl.createControl(TraxUtils.SourceCallback.class);
        TraxUtils.SourceCallback mock = (TraxUtils.SourceCallback) control.getMock();
        mock.staxSource(eventReader);
        control.replay();

        TraxUtils.doWithSource(new StaxSource(eventReader), mock);

        control.verify();
    }

    public void testDoWithStaxResultEventWriter() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(new StringWriter());

        MockControl control = MockControl.createControl(TraxUtils.ResultCallback.class);
        TraxUtils.ResultCallback mock = (TraxUtils.ResultCallback) control.getMock();
        mock.staxResult(eventWriter);
        control.replay();

        TraxUtils.doWithResult(new StaxResult(eventWriter), mock);

        control.verify();
    }

    public void testDoWithStaxSourceStreamReader() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader("<element/>"));

        MockControl control = MockControl.createControl(TraxUtils.SourceCallback.class);
        TraxUtils.SourceCallback mock = (TraxUtils.SourceCallback) control.getMock();
        mock.staxSource(streamReader);
        control.replay();

        TraxUtils.doWithSource(new StaxSource(streamReader), mock);

        control.verify();
    }

    public void testDoWithStaxResultStreamWriter() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(new StringWriter());

        MockControl control = MockControl.createControl(TraxUtils.ResultCallback.class);
        TraxUtils.ResultCallback mock = (TraxUtils.ResultCallback) control.getMock();
        mock.staxResult(streamWriter);
        control.replay();

        TraxUtils.doWithResult(new StaxResult(streamWriter), mock);

        control.verify();
    }

    public void testDoWithStreamSourceInputStream() throws Exception {
        byte[] xml = "<element/>".getBytes("UTF-8");
        InputStream inputStream = new ByteArrayInputStream(xml);

        MockControl control = MockControl.createControl(TraxUtils.SourceCallback.class);
        TraxUtils.SourceCallback mock = (TraxUtils.SourceCallback) control.getMock();
        mock.streamSource(inputStream);
        control.replay();

        TraxUtils.doWithSource(new StreamSource(inputStream), mock);

        control.verify();
    }

    public void testDoWithStreamResultOutputStream() throws Exception {
        OutputStream outputStream = new ByteArrayOutputStream();

        MockControl control = MockControl.createControl(TraxUtils.ResultCallback.class);
        TraxUtils.ResultCallback mock = (TraxUtils.ResultCallback) control.getMock();
        mock.streamResult(outputStream);
        control.replay();

        TraxUtils.doWithResult(new StreamResult(outputStream), mock);

        control.verify();
    }

    public void testDoWithStreamSourceReader() throws Exception {
        String xml = "<element/>";
        Reader reader = new StringReader(xml);

        MockControl control = MockControl.createControl(TraxUtils.SourceCallback.class);
        TraxUtils.SourceCallback mock = (TraxUtils.SourceCallback) control.getMock();
        mock.streamSource(reader);
        control.replay();

        TraxUtils.doWithSource(new StreamSource(reader), mock);

        control.verify();
    }

    public void testDoWithStreamResultWriter() throws Exception {
        Writer writer = new StringWriter();

        MockControl control = MockControl.createControl(TraxUtils.ResultCallback.class);
        TraxUtils.ResultCallback mock = (TraxUtils.ResultCallback) control.getMock();
        mock.streamResult(writer);
        control.replay();

        TraxUtils.doWithResult(new StreamResult(writer), mock);

        control.verify();
    }

    public void testDoWithInvalidSource() throws Exception {
        Source source = new Source() {

            public void setSystemId(String systemId) {
            }

            public String getSystemId() {
                return null;
            }
        };

        try {
            TraxUtils.doWithSource(source, null);
            fail("IllegalArgumentException expected");
        }
        catch (IllegalArgumentException ex) {
            // expected
        }
    }

    public void testDoWithInvalidResult() throws Exception {
        Result result = new Result() {

            public void setSystemId(String systemId) {
            }

            public String getSystemId() {
                return null;
            }
        };

        try {
            TraxUtils.doWithResult(result, null);
            fail("IllegalArgumentException expected");
        }
        catch (IllegalArgumentException ex) {
            // expected
        }
    }
}