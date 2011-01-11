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
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.util.xml.StaxUtils;

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

import static org.easymock.EasyMock.*;

public class TraxUtilsTest {

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

        TraxUtils.SourceCallback mock = createMock(TraxUtils.SourceCallback.class);
        mock.domSource(document);

        replay(mock);

        TraxUtils.doWithSource(new DOMSource(document), mock);

        verify(mock);
    }

    @Test
    public void testDoWithDomResult() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        TraxUtils.ResultCallback mock = createMock(TraxUtils.ResultCallback.class);
        mock.domResult(document);

        replay(mock);

        TraxUtils.doWithResult(new DOMResult(document), mock);

        verify(mock);
    }

    @Test
    public void testDoWithSaxSource() throws Exception {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        InputSource inputSource = new InputSource();

        TraxUtils.SourceCallback mock = createMock(TraxUtils.SourceCallback.class);
        mock.saxSource(reader, inputSource);

        replay(mock);

        TraxUtils.doWithSource(new SAXSource(reader, inputSource), mock);

        verify(mock);
    }

    @Test
    public void testDoWithSaxResult() throws Exception {
        ContentHandler contentHandler = new DefaultHandler();
        LexicalHandler lexicalHandler = new DefaultHandler2();

        TraxUtils.ResultCallback mock = createMock(TraxUtils.ResultCallback.class);
        mock.saxResult(contentHandler, lexicalHandler);

        replay(mock);

        SAXResult result = new SAXResult(contentHandler);
        result.setLexicalHandler(lexicalHandler);
        TraxUtils.doWithResult(result, mock);

        verify(mock);
    }

    @Test
    public void testDoWithStaxSourceEventReader() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader("<element/>"));

        TraxUtils.SourceCallback mock = createMock(TraxUtils.SourceCallback.class);
        mock.staxSource(eventReader);

        replay(mock);

        TraxUtils.doWithSource(StaxUtils.createStaxSource(eventReader), mock);

        verify(mock);
    }

    @Test
    public void testDoWithStaxResultEventWriter() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(new StringWriter());

        TraxUtils.ResultCallback mock = createMock(TraxUtils.ResultCallback.class);
        mock.staxResult(eventWriter);

        replay(mock);

        TraxUtils.doWithResult(StaxUtils.createStaxResult(eventWriter), mock);

        verify(mock);
    }

    @Test
    public void testDoWithStaxSourceStreamReader() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader("<element/>"));

        TraxUtils.SourceCallback mock = createMock(TraxUtils.SourceCallback.class);
        mock.staxSource(streamReader);

        replay(mock);

        TraxUtils.doWithSource(StaxUtils.createStaxSource(streamReader), mock);

        verify(mock);
    }

    @Test
    public void testDoWithStaxResultStreamWriter() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(new StringWriter());

        TraxUtils.ResultCallback mock = createMock(TraxUtils.ResultCallback.class);
        mock.staxResult(streamWriter);

        replay(mock);

        TraxUtils.doWithResult(StaxUtils.createStaxResult(streamWriter), mock);

        verify(mock);
    }

    @Test
    public void testDoWithStreamSourceInputStream() throws Exception {
        byte[] xml = "<element/>".getBytes("UTF-8");
        InputStream inputStream = new ByteArrayInputStream(xml);

        TraxUtils.SourceCallback mock = createMock(TraxUtils.SourceCallback.class);
        mock.streamSource(inputStream);

        replay(mock);

        TraxUtils.doWithSource(new StreamSource(inputStream), mock);

        verify(mock);
    }

    @Test
    public void testDoWithStreamResultOutputStream() throws Exception {
        OutputStream outputStream = new ByteArrayOutputStream();

        TraxUtils.ResultCallback mock = createMock(TraxUtils.ResultCallback.class);
        mock.streamResult(outputStream);

        replay(mock);

        TraxUtils.doWithResult(new StreamResult(outputStream), mock);

        verify(mock);
    }

    @Test
    public void testDoWithStreamSourceReader() throws Exception {
        String xml = "<element/>";
        Reader reader = new StringReader(xml);

        TraxUtils.SourceCallback mock = createMock(TraxUtils.SourceCallback.class);
        mock.streamSource(reader);

        replay(mock);

        TraxUtils.doWithSource(new StreamSource(reader), mock);

        verify(mock);
    }

    @Test
    public void testDoWithStreamResultWriter() throws Exception {
        Writer writer = new StringWriter();

        TraxUtils.ResultCallback mock = createMock(TraxUtils.ResultCallback.class);
        mock.streamResult(writer);

        replay(mock);

        TraxUtils.doWithResult(new StreamResult(writer), mock);

        verify(mock);
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