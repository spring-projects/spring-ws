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

package org.springframework.oxm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.util.Assert;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.ext.LexicalHandler;

/**
 * Abstract implementation of the <code>Marshaller</code> and <code>Unmarshaller</code> interface. This implementation
 * inspects the given <code>Source</code> or <code>Result</code>, and defers further handling to overridable template
 * methods.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractMarshaller implements Marshaller, Unmarshaller {

    /**
     * Marshals the object graph with the given root into the provided <code>javax.xml.transform.Result</code>.
     * <p/>
     * This implementation inspects the given result, and calls <code>marshalDomResult</code>,
     * <code>marshalSaxResult</code>, or <code>marshalStreamResult</code>.
     *
     * @param graph  the root of the object graph to marshal
     * @param result the result to marshal to
     * @throws XmlMappingException      if the given object cannot be marshalled to the result
     * @throws IOException              if an I/O exception occurs
     * @throws IllegalArgumentException if <code>result</code> if neither a <code>DOMResult</code>,
     *                                  <code>SAXResult</code>, <code>StreamResult</code>
     * @see #marshalDomResult(Object, javax.xml.transform.dom.DOMResult)
     * @see #marshalSaxResult(Object, javax.xml.transform.sax.SAXResult)
     * @see #marshalStreamResult(Object, javax.xml.transform.stream.StreamResult)
     */
    public final void marshal(Object graph, Result result) throws XmlMappingException, IOException {
        if (result instanceof DOMResult) {
            marshalDomResult(graph, (DOMResult) result);
        }
        else if (result instanceof SAXResult) {
            marshalSaxResult(graph, (SAXResult) result);
        }
        else if (result instanceof StreamResult) {
            marshalStreamResult(graph, (StreamResult) result);
        }
        else {
            throw new IllegalArgumentException(
                    "Result [" + result.getClass().getName() + "] is neither DOMResult, SAXResult, nor StreamResult");
        }
    }

    /**
     * Unmarshals the given provided <code>javax.xml.transform.Source</code> into an object graph.
     * <p/>
     * This implementation inspects the given result, and calls <code>unmarshalDomSource</code>,
     * <code>unmarshalSaxSource</code>, or <code>unmarshalStreamSource</code>.
     *
     * @param source the source to marshal from
     * @return the object graph
     * @throws XmlMappingException      if the given source cannot be mapped to an object
     * @throws IOException              if an I/O Exception occurs
     * @throws IllegalArgumentException if <code>source</code> is neither a <code>DOMSource</code>, a
     *                                  <code>SAXSource</code>, nor a <code>StreamSource</code>
     * @see #unmarshalDomSource(javax.xml.transform.dom.DOMSource)
     * @see #unmarshalSaxSource(javax.xml.transform.sax.SAXSource)
     * @see #unmarshalStreamSource(javax.xml.transform.stream.StreamSource)
     */
    public final Object unmarshal(Source source) throws XmlMappingException, IOException {
        if (source instanceof DOMSource) {
            return unmarshalDomSource((DOMSource) source);
        }
        else if (source instanceof SAXSource) {
            return unmarshalSaxSource((SAXSource) source);
        }
        else if (source instanceof StreamSource) {
            return unmarshalStreamSource((StreamSource) source);
        }
        else {
            throw new IllegalArgumentException(
                    "Source [" + source.getClass().getName() + "] is neither SAXSource, DOMSource, nor StreamSource");
        }
    }

    /**
     * Template method for handling <code>DOMResult</code>s. This implementation defers to <code>marshalDomNode</code>.
     *
     * @param graph     the root of the object graph to marshal
     * @param domResult the <code>DOMResult</code>
     * @throws XmlMappingException      if the given object cannot be marshalled to the result
     * @throws IllegalArgumentException if the <code>domResult</code> is empty
     * @see #marshalDomNode(Object, org.w3c.dom.Node, org.w3c.dom.Node)
     */
    protected void marshalDomResult(Object graph, DOMResult domResult) throws XmlMappingException {
        Assert.notNull(domResult.getNode(), "DOMResult does not contain Node");
        marshalDomNode(graph, domResult.getNode(), domResult.getNextSibling());
    }

    /**
     * Template method for handling <code>SAXResult</code>s. This implementation defers to
     * <code>marshalSaxHandlers</code>.
     *
     * @param graph     the root of the object graph to marshal
     * @param saxResult the <code>SAXResult</code>
     * @throws XmlMappingException if the given object cannot be marshalled to the result
     * @see #marshalSaxHandlers(Object, org.xml.sax.ContentHandler, org.xml.sax.ext.LexicalHandler)
     */
    protected void marshalSaxResult(Object graph, SAXResult saxResult) throws XmlMappingException {
        ContentHandler contentHandler = saxResult.getHandler();
        Assert.notNull(contentHandler, "ContentHandler not set on SAXResult");
        LexicalHandler lexicalHandler = saxResult.getLexicalHandler();
        if (lexicalHandler == null && contentHandler instanceof LexicalHandler) {
            lexicalHandler = (LexicalHandler) contentHandler;
        }
        marshalSaxHandlers(graph, contentHandler, lexicalHandler);
    }

    /**
     * Template method for handling <code>StreamResult</code>s. This implementation defers to
     * <code>marshalOutputStream</code>, or <code>marshalWriter</code>, depending on what is contained in the
     * <code>StreamResult</code>
     *
     * @param graph        the root of the object graph to marshal
     * @param streamResult the <code>StreamResult</code>
     * @throws IOException              if an I/O Exception occurs
     * @throws XmlMappingException      if the given object cannot be marshalled to the result
     * @throws IllegalArgumentException if <code>streamResult</code> contains neither <code>OutputStream</code> nor
     *                                  <code>Writer</code>.
     */
    protected void marshalStreamResult(Object graph, StreamResult streamResult)
            throws XmlMappingException, IOException {
        if (streamResult.getOutputStream() != null) {
            marshalOutputStream(graph, streamResult.getOutputStream());
        }
        else if (streamResult.getWriter() != null) {
            marshalWriter(graph, streamResult.getWriter());
        }
        else {
            throw new IllegalArgumentException("StreamResult contains neither OutputStream nor Writer");
        }
    }

    /**
     * Template method for handling <code>DOMSource</code>s. This implementation defers to
     * <code>unmarshalDomNode</code>.
     *
     * @param domSource the <code>DOMSource</code>
     * @return the object graph
     * @throws IllegalArgumentException if the <code>domSource</code> is empty
     * @throws XmlMappingException      if the given source cannot be mapped to an object
     * @see #unmarshalDomNode(org.w3c.dom.Node)
     */
    protected Object unmarshalDomSource(DOMSource domSource) throws XmlMappingException {
        Assert.notNull(domSource.getNode(), "DOMSource does not contain Node");
        return unmarshalDomNode(domSource.getNode());
    }

    /**
     * Template method for handling <code>SAXSource</code>s. This implementation defers to
     * <code>unmarshalInputStream</code>, or <code>unmarshalReader</code>.
     *
     * @param saxSource the <code>SAXSource</code>
     * @return the object graph
     * @throws XmlMappingException if the given source cannot be mapped to an object
     * @throws IOException         if an I/O Exception occurs
     */
    protected Object unmarshalSaxSource(SAXSource saxSource) throws XmlMappingException, IOException {
        InputSource inputSource = saxSource.getInputSource();
        if (inputSource.getByteStream() != null) {
            return unmarshalInputStream(inputSource.getByteStream());
        }
        else if (inputSource.getCharacterStream() != null) {
            return unmarshalReader(inputSource.getCharacterStream());
        }
        else {
            throw new IllegalArgumentException("SAXSource InputSource contains neither InputStream nor Reader");
        }
    }

    /**
     * Template method for handling <code>StreamSource</code>s. This implementation defers to
     * <code>unmarshalInputStream</code>, or <code>unmarshalReader</code>.
     *
     * @param streamSource the <code>StreamSource</code>
     * @return the object graph
     * @throws IOException         if an I/O exception occurs
     * @throws XmlMappingException if the given source cannot be mapped to an object
     */
    protected Object unmarshalStreamSource(StreamSource streamSource) throws XmlMappingException, IOException {
        if (streamSource.getInputStream() != null) {
            return unmarshalInputStream(streamSource.getInputStream());
        }
        else if (streamSource.getReader() != null) {
            return unmarshalReader(streamSource.getReader());
        }
        else {
            throw new IllegalArgumentException("StreamSource contains neither InputStream nor Reader");
        }
    }

    /**
     * Abstract template method for marshalling the given object graph to a DOM <code>Node</code>, specifying the child
     * node where the result nodes should be inserted before.
     * <p/>
     * In practice, node and nextSibling should be a <code>Document</code> node, a <code>DocumentFragment</code> node,
     * or a <code>Element</code> node. In other words, a node that accepts children.
     *
     * @param node        The DOM node that will contain the result tree
     * @param nextSibling The child node where the result nodes should be inserted before. Can be <code>null</code>.
     * @throws XmlMappingException if the given object cannot be marshalled to the DOM node
     * @see org.w3c.dom.Document
     * @see org.w3c.dom.DocumentFragment
     * @see org.w3c.dom.Element
     */
    protected abstract void marshalDomNode(Object graph, Node node, Node nextSibling) throws XmlMappingException;

    /**
     * Abstract template method for marshalling the given object graph to a <code>OutputStream</code>.
     *
     * @param graph        the root of the object graph to marshal
     * @param outputStream the <code>OutputStream</code> to write to
     * @throws XmlMappingException if the given object cannot be marshalled to the writer
     * @throws IOException         if an I/O exception occurs
     */
    protected abstract void marshalOutputStream(Object graph, OutputStream outputStream)
            throws XmlMappingException, IOException;

    /**
     * Abstract template method for marshalling the given object graph to a SAX <code>ContentHandler</code>.
     *
     * @param graph          the root of the object graph to marshal
     * @param contentHandler the SAX <code>ContentHandler</code>
     * @param lexicalHandler the SAX2 <code>LexicalHandler</code>. Can be <code>null</code>.
     * @throws XmlMappingException if the given object cannot be marshalled to the handlers
     */
    protected abstract void marshalSaxHandlers(Object graph,
                                               ContentHandler contentHandler,
                                               LexicalHandler lexicalHandler) throws XmlMappingException;

    /**
     * Abstract template method for marshalling the given object graph to a <code>Writer</code>.
     *
     * @param graph  the root of the object graph to marshal
     * @param writer the <code>Writer</code> to write to
     * @throws XmlMappingException if the given object cannot be marshalled to the writer
     * @throws IOException         if an I/O exception occurs
     */
    protected abstract void marshalWriter(Object graph, Writer writer) throws XmlMappingException, IOException;

    /**
     * Abstract template method for unmarshalling from a given DOM <code>Node</code>.
     *
     * @param node The DOM node that contains the objects to be unmarshalled
     * @throws XmlMappingException if the given DOM node cannot be mapped to an object
     */
    protected abstract Object unmarshalDomNode(Node node) throws XmlMappingException;

    /**
     * Abstract template method for unmarshalling from a given <code>InputStream</code>.
     *
     * @param inputStream the <code>InputStreamStream</code> to read from
     * @throws XmlMappingException if the given object cannot be mapped to an object
     * @throws IOException         if an I/O exception occurs
     */
    protected abstract Object unmarshalInputStream(InputStream inputStream) throws XmlMappingException, IOException;

    /**
     * Abstract template method for unmarshalling from a given <code>Reader</code>.
     *
     * @param reader the <code>Reader</code> to read from
     * @throws XmlMappingException if the given object cannot be mapped to an object
     * @throws IOException         if an I/O exception occurs
     */
    protected abstract Object unmarshalReader(Reader reader) throws XmlMappingException, IOException;
}
