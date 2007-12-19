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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import org.springframework.util.Assert;
import org.springframework.xml.JaxpVersion;

/**
 * Convenient utility methods for dealing with TrAX.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public abstract class TraxUtils {

    private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    static {
        documentBuilderFactory.setNamespaceAware(true);
    }

    /**
     * Creates a StAX {@link Source} for the given {@link XMLStreamReader}. Returns a {@link StAXSource} under JAXP 1.4
     * or higher, or a {@link StaxSource} otherwise.
     *
     * @param streamReader the StAX stream reader
     * @return a source wrapping <code>streamReader</code>
     */
    public static Source createStaxSource(XMLStreamReader streamReader) {
        if (JaxpVersion.isAtLeastJaxp14()) {
            return Jaxp14StaxHandler.createStaxSource(streamReader);
        }
        else {
            return new StaxSource(streamReader);
        }
    }

    /**
     * Creates a StAX {@link Source} for the given {@link XMLEventReader}. Returns a {@link StAXSource} under JAXP 1.4
     * or higher, or a {@link StaxSource} otherwise.
     *
     * @param eventReader the StAX event reader
     * @return a source wrapping <code>streamReader</code>
     * @throws XMLStreamException in case of StAX errors
     */
    public static Source createStaxSource(XMLEventReader eventReader) throws XMLStreamException {
        if (JaxpVersion.isAtLeastJaxp14()) {
            return Jaxp14StaxHandler.createStaxSource(eventReader);
        }
        else {
            return new StaxSource(eventReader);
        }
    }

    /**
     * Handles the given {@link Source} by calling one of the methods on the given {@link SourceHandler}.
     *
     * @param source  the source to handle
     * @param handler the handler
     */
    public static void handleSource(Source source, SourceHandler handler) {
        if (source instanceof DOMSource) {
            handleDomSource((DOMSource) source, handler);
        }
        else if (isStaxSource(source)) {
            handleStaxSource(source, handler);
        }
        else if (source instanceof SAXSource) {
            handleSaxSource((SAXSource) source, handler);
        }
        else if (source instanceof StreamSource) {
            handleStreamSource((StreamSource) source, handler);
        }
        else {
            throw new IllegalArgumentException("Unknown Source type: " + source.getClass());
        }
    }

    private static boolean isStaxSource(Source source) {
        if (source instanceof StaxSource) {
            return true;
        }
        else if (JaxpVersion.isAtLeastJaxp14()) {
            return Jaxp14StaxHandler.isStaxSource(source);
        }
        else {
            return false;
        }
    }

    private static void handleDomSource(DOMSource domSource, SourceHandler handler) {
        if (domSource.getNode() == null) {
            try {
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                domSource.setNode(documentBuilder.newDocument());
            }
            catch (ParserConfigurationException ex) {
                throw new IllegalStateException("Could not create document for DOMSource: " + ex.getMessage(), ex);
            }
        }
        handler.domSource(domSource.getNode());
    }

    private static void handleStaxSource(Source source, SourceHandler handler) {
        XMLStreamReader streamReader = null;
        XMLEventReader eventReader = null;
        if (source instanceof StaxSource) {
            StaxSource staxSource = (StaxSource) source;
            streamReader = staxSource.getXMLStreamReader();
            eventReader = staxSource.getXMLEventReader();
        }
        else if (JaxpVersion.isAtLeastJaxp14()) {
            streamReader = Jaxp14StaxHandler.getXMLStreamReader(source);
            eventReader = Jaxp14StaxHandler.getXMLEventReader(source);
        }
        if (streamReader != null) {
            handler.staxSource(streamReader);
        }
        else if (eventReader != null) {
            handler.staxSource(eventReader);
        }
        else {
            throw new IllegalArgumentException("StAX Source contains neither XMLStreamReader nor XMLEventReader");
        }
    }

    private static void handleSaxSource(SAXSource saxSource, SourceHandler handler) {
        if (saxSource.getXMLReader() == null) {
            try {
                saxSource.setXMLReader(XMLReaderFactory.createXMLReader());
            }
            catch (SAXException ex) {
                throw new IllegalStateException("Could not create XMLReader for SAXSource: " + ex.getMessage(), ex);
            }
        }
        if (saxSource.getInputSource() == null) {
            saxSource.setInputSource(new InputSource());
        }
        handler.saxSource(saxSource.getXMLReader(), saxSource.getInputSource());
    }

    private static void handleStreamSource(StreamSource streamSource, SourceHandler handler) {
        if (streamSource.getInputStream() != null) {
            handler.streamSource(streamSource.getInputStream());
        }
        else if (streamSource.getReader() != null) {
            handler.streamSource(streamSource.getReader());
        }
        else {
            throw new IllegalArgumentException("StreamSource contains neither InputStream nor Reader");
        }
    }

    /**
     * Handles the given {@link Result} by calling one of the methods on the given {@link ResultHandler}.
     *
     * @param result  the result to handle
     * @param handler the handler
     */
    public static void handleResult(Result result, ResultHandler handler) {
        if (result instanceof DOMResult) {
            handleDomResult((DOMResult) result, handler);
        }
        else if (isStaxResult(result)) {
            handleStaxResult(result, handler);
        }
        else if (result instanceof SAXResult) {
            handleSaxResult((SAXResult) result, handler);
        }
        else if (result instanceof StreamResult) {
            handleStreamResult((StreamResult) result, handler);
        }
        else {
            throw new IllegalArgumentException("Unknown Result type: " + result.getClass());
        }
    }

    private static boolean isStaxResult(Result result) {
        if (result instanceof StaxResult) {
            return true;
        }
        else if (JaxpVersion.isAtLeastJaxp14()) {
            return Jaxp14StaxHandler.isStaxResult(result);
        }
        else {
            return false;
        }
    }

    private static void handleDomResult(DOMResult domResult, ResultHandler handler) {
        if (domResult.getNode() == null) {
            try {
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                domResult.setNode(documentBuilder.newDocument());
            }
            catch (ParserConfigurationException ex) {
                throw new IllegalStateException("Could not create document for DOMResult: " + ex.getMessage(), ex);
            }
        }
        handler.domResult(domResult.getNode());
    }

    private static void handleStaxResult(Result result, ResultHandler handler) {
        XMLStreamWriter streamWriter = null;
        XMLEventWriter eventWriter = null;
        if (result instanceof StaxResult) {
            StaxResult staxResult = (StaxResult) result;
            streamWriter = staxResult.getXMLStreamWriter();
            eventWriter = staxResult.getXMLEventWriter();
        }
        else if (JaxpVersion.isAtLeastJaxp14()) {
            streamWriter = Jaxp14StaxHandler.getXMLStreamWriter(result);
            eventWriter = Jaxp14StaxHandler.getXMLEventWriter(result);
        }
        if (streamWriter != null) {
            handler.staxResult(streamWriter);
        }
        else if (eventWriter != null) {
            handler.staxResult(eventWriter);
        }
        else {
            throw new IllegalArgumentException("StAX Result contains neither XMLStreamWriter nor XMLEventWriter");
        }
    }

    private static void handleSaxResult(SAXResult saxResult, ResultHandler handler) {
        ContentHandler contentHandler = saxResult.getHandler();
        if (contentHandler == null) {
            contentHandler = new DefaultHandler();
        }
        LexicalHandler lexicalHandler = saxResult.getLexicalHandler();
        handler.saxResult(contentHandler, lexicalHandler);
    }

    private static void handleStreamResult(StreamResult streamResult, ResultHandler handler) {
        if (streamResult.getOutputStream() != null) {
            handler.streamResult(streamResult.getOutputStream());
        }
        else if (streamResult.getWriter() != null) {
            handler.streamResult(streamResult.getWriter());
        }
        else {
            throw new IllegalArgumentException("StreamResult contains neither OutputStream nor Writer");
        }
    }

    /** Inner class to avoid a static JAXP 1.4 dependency. */
    private static class Jaxp14StaxHandler {

        private static Source createStaxSource(XMLStreamReader streamReader) {
            return new StAXSource(streamReader);
        }

        private static Source createStaxSource(XMLEventReader eventReader) throws XMLStreamException {
            return new StAXSource(eventReader);
        }

        private static boolean isStaxSource(Source source) {
            return source instanceof StAXSource;
        }

        private static boolean isStaxResult(Result result) {
            return result instanceof StAXResult;
        }

        private static XMLStreamReader getXMLStreamReader(Source source) {
            Assert.isInstanceOf(StAXSource.class, source);
            return ((StAXSource) source).getXMLStreamReader();
        }

        private static XMLEventReader getXMLEventReader(Source source) {
            Assert.isInstanceOf(StAXSource.class, source);
            return ((StAXSource) source).getXMLEventReader();
        }

        private static XMLStreamWriter getXMLStreamWriter(Result result) {
            Assert.isInstanceOf(StAXResult.class, result);
            return ((StAXResult) result).getXMLStreamWriter();
        }

        private static XMLEventWriter getXMLEventWriter(Result result) {
            Assert.isInstanceOf(StAXResult.class, result);
            return ((StAXResult) result).getXMLEventWriter();
        }
    }


}
