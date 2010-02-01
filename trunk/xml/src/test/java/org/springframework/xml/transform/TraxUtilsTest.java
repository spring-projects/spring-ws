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

import org.easymock.MockControl;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

@SuppressWarnings("Since15")
public class TraxUtilsTest {

    @Test
    public void testIsStaxSourceInvalid() throws Exception {
        Assert.assertFalse("A StAX Source", TraxUtils.isStaxSource(new DOMSource()));
        Assert.assertFalse("A StAX Source", TraxUtils.isStaxSource(new SAXSource()));
        Assert.assertFalse("A StAX Source", TraxUtils.isStaxSource(new StreamSource()));
    }

    @Test
    public void testIsStaxSource() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        String expected = "<element/>";
        XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader(expected));
        StaxSource source = new StaxSource(streamReader);

        Assert.assertTrue("Not a StAX Source", TraxUtils.isStaxSource(source));
    }

    @Test
    public void testIsStaxSourceJaxp14() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        String expected = "<element/>";
        XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader(expected));
        StAXSource source = new StAXSource(streamReader);

        Assert.assertTrue("Not a StAX Source", TraxUtils.isStaxSource(source));
    }

    @Test
    public void testIsStaxResultInvalid() throws Exception {
        Assert.assertFalse("A StAX Result", TraxUtils.isStaxResult(new DOMResult()));
        Assert.assertFalse("A StAX Result", TraxUtils.isStaxResult(new SAXResult()));
        Assert.assertFalse("A StAX Result", TraxUtils.isStaxResult(new StreamResult()));
    }

    @Test
    public void testIsStaxResult() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(new StringWriter());
        StaxResult result = new StaxResult(streamWriter);

        Assert.assertTrue("Not a StAX Result", TraxUtils.isStaxResult(result));
    }

    @Test
    public void testIsStaxResultJaxp14() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(new StringWriter());
        StAXResult result = new StAXResult(streamWriter);

        Assert.assertTrue("Not a StAX Result", TraxUtils.isStaxResult(result));
    }

    @Test
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

    @Test
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

    @Test
    public void testGetXMLStreamReader() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        String expected = "<element/>";
        XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader(expected));

        StaxSource source = new StaxSource(streamReader);

        Assert.assertEquals("Invalid XMLStreamReader", streamReader, TraxUtils.getXMLStreamReader(source));
    }

    @Test
    public void testGetXMLStreamReaderJaxp14() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        String expected = "<element/>";
        XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader(expected));

        StAXSource source = new StAXSource(streamReader);

        Assert.assertEquals("Invalid XMLStreamReader", streamReader, TraxUtils.getXMLStreamReader(source));
    }

    @Test
    public void testGetXMLEventReader() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        String expected = "<element/>";
        XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader(expected));

        StaxSource source = new StaxSource(eventReader);

        Assert.assertEquals("Invalid XMLEventReader", eventReader, TraxUtils.getXMLEventReader(source));
    }

    @Test
    public void testGetXMLEventReaderJaxp14() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        String expected = "<element/>";
        XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader(expected));

        StAXSource source = new StAXSource(eventReader);

        Assert.assertEquals("Invalid XMLEventReader", eventReader, TraxUtils.getXMLEventReader(source));
    }

    @Test
    public void testGetXMLStreamWriter() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(new StringWriter());

        StaxResult result = new StaxResult(streamWriter);

        Assert.assertEquals("Invalid XMLStreamWriter", streamWriter, TraxUtils.getXMLStreamWriter(result));
    }

    @Test
    public void testGetXMLStreamWriterJaxp14() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(new StringWriter());

        StAXResult result = new StAXResult(streamWriter);

        Assert.assertEquals("Invalid XMLStreamWriter", streamWriter, TraxUtils.getXMLStreamWriter(result));
    }

    @Test
    public void testGetXMLEventWriter() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(new StringWriter());

        StaxResult result = new StaxResult(eventWriter);

        Assert.assertEquals("Invalid XMLStreamWriter", eventWriter, TraxUtils.getXMLEventWriter(result));
    }

    @Test
    public void testGetXMLEventWriterJaxp14() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(new StringWriter());

        StAXResult result = new StAXResult(eventWriter);

        Assert.assertEquals("Invalid XMLEventWriter", eventWriter, TraxUtils.getXMLEventWriter(result));
    }

    @Test
    public void testGetDocument() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Assert.assertSame("Invalid document", document, TraxUtils.getDocument(new DOMSource(document)));
        Element element = document.createElement("element");
        document.appendChild(element);
        Assert.assertSame("Invalid document", document, TraxUtils.getDocument(new DOMSource(element)));
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testDoWithStreamResultOutputStream() throws Exception {
        OutputStream outputStream = new ByteArrayOutputStream();

        MockControl control = MockControl.createControl(TraxUtils.ResultCallback.class);
        TraxUtils.ResultCallback mock = (TraxUtils.ResultCallback) control.getMock();
        mock.streamResult(outputStream);
        control.replay();

        TraxUtils.doWithResult(new StreamResult(outputStream), mock);

        control.verify();
    }

    @Test
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

    @Test
    public void testDoWithStreamResultWriter() throws Exception {
        Writer writer = new StringWriter();

        MockControl control = MockControl.createControl(TraxUtils.ResultCallback.class);
        TraxUtils.ResultCallback mock = (TraxUtils.ResultCallback) control.getMock();
        mock.streamResult(writer);
        control.replay();

        TraxUtils.doWithResult(new StreamResult(writer), mock);

        control.verify();
    }

    @Test
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
            Assert.fail("IllegalArgumentException expected");
        }
        catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
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
            Assert.fail("IllegalArgumentException expected");
        }
        catch (IllegalArgumentException ex) {
            // expected
        }
    }
}