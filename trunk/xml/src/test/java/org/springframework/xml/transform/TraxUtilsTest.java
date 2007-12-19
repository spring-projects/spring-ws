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
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class TraxUtilsTest extends XMLTestCase {

    private SourceHandler sourceHandlerMock;

    private MockControl sourceHandlerControl;

    private MockControl resultHandlerControl;

    private ResultHandler resultHandlerMock;

    protected void setUp() throws Exception {
        sourceHandlerControl = MockControl.createControl(SourceHandler.class);
        sourceHandlerMock = (SourceHandler) sourceHandlerControl.getMock();
        resultHandlerControl = MockControl.createControl(ResultHandler.class);
        resultHandlerMock = (ResultHandler) resultHandlerControl.getMock();
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

    public void testHandleDomSource() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        DOMSource domSource = new DOMSource(document);
        sourceHandlerMock.domSource(domSource.getNode());
        sourceHandlerControl.replay();

        TraxUtils.handleSource(domSource, sourceHandlerMock);

        sourceHandlerControl.verify();
    }

    public void testHandleEmptyDomSource() throws Exception {
        DOMSource domSource = new DOMSource();
        sourceHandlerMock.domSource(domSource.getNode());
        sourceHandlerControl.setMatcher(MockControl.ALWAYS_MATCHER);
        sourceHandlerControl.replay();

        TraxUtils.handleSource(domSource, sourceHandlerMock);

        sourceHandlerControl.verify();
    }

    public void testHandlerJaxp14StaxSourceStreamReader() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader("<element/>"));

        StAXSource staxSource = new StAXSource(streamReader);
        sourceHandlerMock.staxSource(streamReader);
        sourceHandlerControl.replay();

        TraxUtils.handleSource(staxSource, sourceHandlerMock);

        sourceHandlerControl.verify();
    }

    public void testHandlerJaxp14StaxSourceEventReader() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader("<element/>"));

        StAXSource staxSource = new StAXSource(eventReader);
        sourceHandlerMock.staxSource(eventReader);
        sourceHandlerControl.replay();

        TraxUtils.handleSource(staxSource, sourceHandlerMock);

        sourceHandlerControl.verify();
    }

    public void testHandlerStaxSourceStreamReader() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader("<element/>"));

        StaxSource staxSource = new StaxSource(streamReader);
        sourceHandlerMock.staxSource(streamReader);
        sourceHandlerControl.replay();

        TraxUtils.handleSource(staxSource, sourceHandlerMock);

        sourceHandlerControl.verify();
    }

    public void testHandlerStaxSourceEventReader() throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader("<element/>"));

        StaxSource staxSource = new StaxSource(eventReader);
        sourceHandlerMock.staxSource(eventReader);
        sourceHandlerControl.replay();

        TraxUtils.handleSource(staxSource, sourceHandlerMock);

        sourceHandlerControl.verify();
    }

    public void testHandlerSaxSource() throws Exception {
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        InputSource inputSource = new InputSource(new StringReader("<element/>"));

        SAXSource saxSource = new SAXSource(xmlReader, inputSource);
        sourceHandlerMock.saxSource(xmlReader, inputSource);
        sourceHandlerControl.replay();

        TraxUtils.handleSource(saxSource, sourceHandlerMock);

        sourceHandlerControl.verify();
    }

    public void testHandlerEmptySaxSource() throws Exception {
        SAXSource saxSource = new SAXSource();
        sourceHandlerMock.saxSource(null, null);
        sourceHandlerControl.setMatcher(MockControl.ALWAYS_MATCHER);
        sourceHandlerControl.replay();

        TraxUtils.handleSource(saxSource, sourceHandlerMock);

        sourceHandlerControl.verify();
    }

    public void testHandleStreamSourceInputStream() throws Exception {
        InputStream inputStream = new ByteArrayInputStream("<element/>".getBytes("UTF-8"));

        StreamSource streamSource = new StreamSource(inputStream);
        sourceHandlerMock.streamSource(inputStream);
        sourceHandlerControl.replay();

        TraxUtils.handleSource(streamSource, sourceHandlerMock);

        sourceHandlerControl.verify();
    }

    public void testHandleStreamSourceReader() throws Exception {
        Reader reader = new StringReader("<element/>");

        StreamSource streamSource = new StreamSource(reader);
        sourceHandlerMock.streamSource(reader);
        sourceHandlerControl.replay();

        TraxUtils.handleSource(streamSource, sourceHandlerMock);

        sourceHandlerControl.verify();
    }

    public void testHandleDomResult() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        DOMResult domResult = new DOMResult(document);
        resultHandlerMock.domResult(domResult.getNode());
        resultHandlerControl.replay();

        TraxUtils.handleResult(domResult, resultHandlerMock);

        resultHandlerControl.verify();
    }

    public void testHandleEmptyDomResult() throws Exception {
        DOMResult domResult = new DOMResult();
        resultHandlerMock.domResult(domResult.getNode());
        resultHandlerControl.setMatcher(MockControl.ALWAYS_MATCHER);
        resultHandlerControl.replay();

        TraxUtils.handleResult(domResult, resultHandlerMock);

        resultHandlerControl.verify();
    }

    public void testHandlerJaxp14StaxResultStreamReader() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(new StringWriter());

        StAXResult stAXResult = new StAXResult(streamWriter);
        resultHandlerMock.staxResult(streamWriter);
        resultHandlerControl.replay();

        TraxUtils.handleResult(stAXResult, resultHandlerMock);

        resultHandlerControl.verify();
    }

    public void testHandlerJaxp14StaxResultEventReader() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(new StringWriter());

        StAXResult stAXResult = new StAXResult(eventWriter);
        resultHandlerMock.staxResult(eventWriter);
        resultHandlerControl.replay();

        TraxUtils.handleResult(stAXResult, resultHandlerMock);

        resultHandlerControl.verify();
    }

    public void testHandlerStaxResultStreamReader() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(new StringWriter());

        StaxResult staxResult = new StaxResult(streamWriter);
        resultHandlerMock.staxResult(streamWriter);
        resultHandlerControl.replay();

        TraxUtils.handleResult(staxResult, resultHandlerMock);

        resultHandlerControl.verify();
    }

    public void testHandlerStaxResultEventReader() throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(new StringWriter());

        StaxResult staxResult = new StaxResult(eventWriter);
        resultHandlerMock.staxResult(eventWriter);
        resultHandlerControl.replay();

        TraxUtils.handleResult(staxResult, resultHandlerMock);

        resultHandlerControl.verify();
    }

    public void testHandlerSaxResult() throws Exception {
        ContentHandler contentHandler = new DefaultHandler();
        LexicalHandler lexicalHandler = new DefaultHandler2();

        SAXResult saxResult = new SAXResult(contentHandler);
        saxResult.setLexicalHandler(lexicalHandler);
        resultHandlerMock.saxResult(contentHandler, lexicalHandler);
        resultHandlerControl.replay();

        TraxUtils.handleResult(saxResult, resultHandlerMock);

        resultHandlerControl.verify();
    }

    public void testHandlerEmptySaxResult() throws Exception {
        SAXResult saxResult = new SAXResult();
        resultHandlerMock.saxResult(null, null);
        resultHandlerControl.setMatcher(MockControl.ALWAYS_MATCHER);
        resultHandlerControl.replay();

        TraxUtils.handleResult(saxResult, resultHandlerMock);

        resultHandlerControl.verify();
    }

    public void testHandleStreamResultOutputStream() throws Exception {
        OutputStream outputStream = new ByteArrayOutputStream();

        StreamResult streamResult = new StreamResult(outputStream);
        resultHandlerMock.streamResult(outputStream);
        resultHandlerControl.replay();

        TraxUtils.handleResult(streamResult, resultHandlerMock);

        resultHandlerControl.verify();
    }

    public void testHandleStreamResultWriter() throws Exception {
        Writer writer = new StringWriter();

        StreamResult streamResult = new StreamResult(writer);
        resultHandlerMock.streamResult(writer);
        resultHandlerControl.replay();

        TraxUtils.handleResult(streamResult, resultHandlerMock);

        resultHandlerControl.verify();
    }

}