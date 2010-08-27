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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
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

import org.springframework.util.Assert;
import org.springframework.xml.JaxpVersion;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * Convenient utility methods for dealing with TrAX.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public abstract class TraxUtils {

    /**
     * Indicates whether the given {@link Source} is a StAX Source.
     *
     * @return <code>true</code> if <code>source</code> is a Spring-WS {@link StaxSource} or JAXP 1.4 {@link
     *         StAXSource}; <code>false</code> otherwise.
     * @deprecated In favor of {@link org.springframework.util.xml.StaxUtils#isStaxSource(Source)}
     */
    @Deprecated
    public static boolean isStaxSource(Source source) {
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

    /**
     * Indicates whether the given {@link Result} is a StAX Result.
     *
     * @return <code>true</code> if <code>result</code> is a Spring-WS {@link StaxResult} or JAXP 1.4 {@link
     *         StAXResult}; <code>false</code> otherwise.
     * @deprecated In favor of {@link org.springframework.util.xml.StaxUtils#isStaxResult(Result)}
     */
    @Deprecated
    public static boolean isStaxResult(Result result) {
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

    /**
     * Returns the {@link XMLStreamReader} for the given StAX Source.
     *
     * @param source a Spring-WS {@link StaxSource} or {@link StAXSource}
     * @return the {@link XMLStreamReader}
     * @throws IllegalArgumentException if <code>source</code> is neither a Spring-WS {@link StaxSource} or {@link
     *                                  StAXSource}
     * @deprecated In favor of {@link org.springframework.util.xml.StaxUtils#getXMLStreamReader(Source)}
     */
    @Deprecated
    public static XMLStreamReader getXMLStreamReader(Source source) {
        if (source instanceof StaxSource) {
            return ((StaxSource) source).getXMLStreamReader();
        }
        else if (JaxpVersion.isAtLeastJaxp14()) {
            return Jaxp14StaxHandler.getXMLStreamReader(source);
        }
        else {
            throw new IllegalArgumentException("Source '" + source + "' is neither StaxSource nor StAXSource");
        }
    }

    /**
     * Returns the {@link XMLEventReader} for the given StAX Source.
     *
     * @param source a Spring-WS {@link StaxSource} or {@link StAXSource}
     * @return the {@link XMLEventReader}
     * @throws IllegalArgumentException if <code>source</code> is neither a Spring-WS {@link StaxSource} or {@link
     *                                  StAXSource}
     * @deprecated In favor of {@link org.springframework.util.xml.StaxUtils#getXMLEventReader(Source)}
     */
    @Deprecated
    public static XMLEventReader getXMLEventReader(Source source) {
        if (source instanceof StaxSource) {
            return ((StaxSource) source).getXMLEventReader();
        }
        else if (JaxpVersion.isAtLeastJaxp14()) {
            return Jaxp14StaxHandler.getXMLEventReader(source);
        }
        else {
            throw new IllegalArgumentException("Source '" + source + "' is neither StaxSource nor StAXSource");
        }
    }

    /**
     * Returns the {@link XMLStreamWriter} for the given StAX Result.
     *
     * @param result a Spring-WS {@link StaxResult} or {@link StAXResult}
     * @return the {@link XMLStreamReader}
     * @throws IllegalArgumentException if <code>source</code> is neither a Spring-WS {@link StaxResult} or {@link
     *                                  StAXResult}
     * @deprecated In favor of {@link org.springframework.util.xml.StaxUtils#getXMLStreamWriter(Result)}
     */
    @Deprecated
    public static XMLStreamWriter getXMLStreamWriter(Result result) {
        if (result instanceof StaxResult) {
            return ((StaxResult) result).getXMLStreamWriter();
        }
        else if (JaxpVersion.isAtLeastJaxp14()) {
            return Jaxp14StaxHandler.getXMLStreamWriter(result);
        }
        else {
            throw new IllegalArgumentException("Result '" + result + "' is neither StaxResult nor StAXResult");
        }
    }

    /**
     * Returns the {@link XMLEventWriter} for the given StAX Result.
     *
     * @param result a Spring-WS {@link StaxResult} or {@link StAXResult}
     * @return the {@link XMLStreamReader}
     * @throws IllegalArgumentException if <code>source</code> is neither a Spring-WS {@link StaxResult} or {@link
     *                                  StAXResult}
     * @deprecated In favor of {@link org.springframework.util.xml.StaxUtils#getXMLEventWriter(Result)}
     */
    @Deprecated
    public static XMLEventWriter getXMLEventWriter(Result result) {
        if (result instanceof StaxResult) {
            return ((StaxResult) result).getXMLEventWriter();
        }
        else if (JaxpVersion.isAtLeastJaxp14()) {
            return Jaxp14StaxHandler.getXMLEventWriter(result);
        }
        else {
            throw new IllegalArgumentException("Result '" + result + "' is neither StaxResult nor StAXResult");
        }
    }

    /**
     * Creates a StAX {@link Source} for the given {@link XMLStreamReader}. Returns a {@link StAXSource} under JAXP 1.4
     * or higher, or a {@link StaxSource} otherwise.
     *
     * @param streamReader the StAX stream reader
     * @return a source wrapping <code>streamReader</code>
     * @deprecated In favor of {@link org.springframework.util.xml.StaxUtils#createStaxSource(XMLStreamReader)}
     */
    @Deprecated
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
     * @return a source wrapping <code>eventReader</code>
     * @throws XMLStreamException in case of StAX errors
     * @deprecated In favor of {@link org.springframework.util.xml.StaxUtils#createStaxSource(XMLEventReader)}
     */
    @Deprecated
    public static Source createStaxSource(XMLEventReader eventReader) throws XMLStreamException {
        if (JaxpVersion.isAtLeastJaxp14()) {
            return Jaxp14StaxHandler.createStaxSource(eventReader);
        }
        else {
            return new StaxSource(eventReader);
        }
    }

    /**
     * Returns the {@link Document} of the given {@link DOMSource}.
     *
     * @param source the DOM source
     * @return the document
     */
    public static Document getDocument(DOMSource source) {
        Node node = source.getNode();
        if (node instanceof Document) {
            return (Document) node;
        }
        else if (node != null) {
            return node.getOwnerDocument();
        }
        else {
            return null;
        }
    }

    /**
     * Performs the given {@linkplain SourceCallback callback} operation on a {@link Source}. Supports both the JAXP 1.4
     * {@link StAXSource} and the Spring-WS {@link StaxSource}.
     *
     * @param source   source to look at
     * @param callback the callback to invoke for each kind of source
     */
    public static void doWithSource(Source source, SourceCallback callback) throws Exception {
        if (source instanceof DOMSource) {
            callback.domSource(((DOMSource) source).getNode());
        }
        else if (isStaxSource(source)) {
            XMLStreamReader streamReader = getXMLStreamReader(source);
            if (streamReader != null) {
                callback.staxSource(streamReader);
            }
            else {
                XMLEventReader eventReader = getXMLEventReader(source);
                if (eventReader != null) {
                    callback.staxSource(eventReader);
                }
                else {
                    throw new IllegalArgumentException(
                            "StAX source contains neither XMLStreamReader nor XMLEventReader");
                }
            }
        }
        else if (source instanceof SAXSource) {
            SAXSource saxSource = (SAXSource) source;
            callback.saxSource(saxSource.getXMLReader(), saxSource.getInputSource());
        }
        else if (source instanceof StreamSource) {
            StreamSource streamSource = (StreamSource) source;
            if (streamSource.getInputStream() != null) {
                callback.streamSource(streamSource.getInputStream());
            }
            else if (streamSource.getReader() != null) {
                callback.streamSource(streamSource.getReader());
            }
            else {
                throw new IllegalArgumentException("StreamSource contains neither InputStream nor Reader");
            }
        }
        else {
            throw new IllegalArgumentException("Unknown Source type: " + source.getClass());
        }
    }

    /**
     * Performs the given {@linkplain ResultCallback callback} operation on a {@link Result}. Supports both the JAXP 1.4
     * {@link StAXResult} and the Spring-WS {@link StaxResult}.
     *
     * @param result   result to look at
     * @param callback the callback to invoke for each kind of result
     */
    public static void doWithResult(Result result, ResultCallback callback) throws Exception{
        if (result instanceof DOMResult) {
            callback.domResult(((DOMResult) result).getNode());
        }
        else if (isStaxResult(result)) {
            XMLStreamWriter streamWriter = getXMLStreamWriter(result);
            if (streamWriter != null) {
                callback.staxResult(streamWriter);
            }
            else {
                XMLEventWriter eventWriter = getXMLEventWriter(result);
                if (eventWriter != null) {
                    callback.staxResult(eventWriter);
                }
                else {
                    throw new IllegalArgumentException(
                            "StAX result contains neither XMLStreamWriter nor XMLEventWriter");
                }
            }
        }
        else if (result instanceof SAXResult) {
            SAXResult saxSource = (SAXResult) result;
            callback.saxResult(saxSource.getHandler(), saxSource.getLexicalHandler());
        }
        else if (result instanceof StreamResult) {
            StreamResult streamSource = (StreamResult) result;
            if (streamSource.getOutputStream() != null) {
                callback.streamResult(streamSource.getOutputStream());
            }
            else if (streamSource.getWriter() != null) {
                callback.streamResult(streamSource.getWriter());
            }
            else {
                throw new IllegalArgumentException("StreamResult contains neither OutputStream nor Writer");
            }
        }
        else {
            throw new IllegalArgumentException("Unknown Result type: " + result.getClass());
        }
    }

    /**
     * Callback interface invoked on each sort of {@link Source}.
     *
     * @see TraxUtils#doWithSource(Source, SourceCallback)
     */
    public interface SourceCallback {

        /**
         * Perform an operation on the node contained in a {@link DOMSource}.
         *
         * @param node the node
         */
        void domSource(Node node) throws Exception;

        /**
         * Perform an operation on the {@code XMLReader} and {@code InputSource} contained in a {@link SAXSource}.
         *
         * @param reader      the reader, can be {@code null}
         * @param inputSource the input source, can be {@code null}
         */
        void saxSource(XMLReader reader, InputSource inputSource) throws Exception;

        /**
         * Perform an operation on the {@code XMLEventReader} contained in a JAXP 1.4 {@link StAXSource} or Spring
         * {@link StaxSource}.
         *
         * @param eventReader the reader
         */
        void staxSource(XMLEventReader eventReader) throws Exception;

        /**
         * Perform an operation on the {@code XMLStreamReader} contained in a JAXP 1.4 {@link StAXSource} or Spring
         * {@link StaxSource}.
         *
         * @param streamReader the reader
         */
        void staxSource(XMLStreamReader streamReader) throws Exception;

        /**
         * Perform an operation on the {@code InputStream} contained in a {@link StreamSource}.
         *
         * @param inputStream the input stream
         */
        void streamSource(InputStream inputStream) throws Exception;

        /**
         * Perform an operation on the {@code Reader} contained in a {@link StreamSource}.
         *
         * @param reader the reader
         */
        void streamSource(Reader reader) throws Exception;
    }

    /**
     * Callback interface invoked on each sort of {@link Result}.
     *
     * @see TraxUtils#doWithResult(Result, ResultCallback)
     */
    public interface ResultCallback {

        /**
         * Perform an operation on the node contained in a {@link DOMResult}.
         *
         * @param node the node
         */
        void domResult(Node node) throws Exception;

        /**
         * Perform an operation on the {@code ContentHandler} and {@code LexicalHandler} contained in a {@link
         * SAXResult}.
         *
         * @param contentHandler the content handler
         * @param lexicalHandler the lexicalHandler, can be {@code null}
         */
        void saxResult(ContentHandler contentHandler, LexicalHandler lexicalHandler) throws Exception;

        /**
         * Perform an operation on the {@code XMLEventWriter} contained in a JAXP 1.4 {@link StAXResult} or Spring
         * {@link StaxResult}.
         *
         * @param eventWriter the writer
         */
        void staxResult(XMLEventWriter eventWriter) throws Exception;

        /**
         * Perform an operation on the {@code XMLStreamWriter} contained in a JAXP 1.4 {@link StAXResult} or Spring
         * {@link StaxResult}.
         *
         * @param streamWriter the writer
         */
        void staxResult(XMLStreamWriter streamWriter) throws Exception;

        /**
         * Perform an operation on the {@code OutputStream} contained in a {@link StreamResult}.
         *
         * @param outputStream the output stream
         */
        void streamResult(OutputStream outputStream) throws Exception;

        /**
         * Perform an operation on the {@code Writer} contained in a {@link StreamResult}.
         *
         * @param writer the writer
         */
        void streamResult(Writer writer) throws Exception;
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
