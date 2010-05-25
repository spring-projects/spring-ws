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

package org.springframework.ws.server.endpoint;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Locale;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.springframework.core.NestedRuntimeException;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.TransformerObjectSupport;
import org.springframework.xml.transform.TraxUtils;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.NodeFactory;
import nu.xom.ParentNode;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;
import nu.xom.converters.DOMConverter;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Abstract base class for endpoints that handle the message payload as XOM elements. Offers the message payload as a
 * XOM <code>Element</code>, and allows subclasses to create a response by returning an <code>Element</code>.
 * <p/>
 * An <code>AbstractXomPayloadEndpoint</code> only accept one payload element. Multiple payload elements are not in
 * accordance with WS-I.
 *
 * @author Arjen Poutsma
 * @see Element
 * @since 1.0.0
 * @deprecated as of Spring Web Services 2.0, in favor of annotated endpoints
 */
@Deprecated
@SuppressWarnings("Since15")
public abstract class AbstractXomPayloadEndpoint extends TransformerObjectSupport implements PayloadEndpoint {

    public final Source invoke(Source request) throws Exception {
        Element requestElement = null;
        if (request != null) {
            XomSourceCallback sourceCallback = new XomSourceCallback();
            try {
                TraxUtils.doWithSource(request, sourceCallback);
            }
            catch (XomParsingException ex) {
                throw (ParsingException) ex.getCause();
            }
            requestElement = sourceCallback.element;
        }
        Element responseElement = invokeInternal(requestElement);
        return responseElement != null ? convertResponse(responseElement) : null;
    }

    private Source convertResponse(Element responseElement) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Serializer serializer = createSerializer(os);
        Document document = responseElement.getDocument();
        if (document == null) {
            document = new Document(responseElement);
        }
        serializer.write(document);
        byte[] bytes = os.toByteArray();
        return new StreamSource(new ByteArrayInputStream(bytes));
    }

    /**
     * Creates a {@link Serializer} to be used for writing the response to.
     * <p/>
     * Default implementation uses the UTF-8 encoding and does not set any options, but this may be changed in
     * subclasses.
     *
     * @param outputStream the output stream to serialize to
     * @return the serializer
     */
    protected Serializer createSerializer(OutputStream outputStream) {
        return new Serializer(outputStream);
    }

    /**
     * Template method. Subclasses must implement this. Offers the request payload as a XOM <code>Element</code>, and
     * allows subclasses to return a response <code>Element</code>.
     *
     * @param requestElement the contents of the SOAP message as XOM element
     * @return the response element. Can be <code>null</code> to specify no response.
     */
    protected abstract Element invokeInternal(Element requestElement) throws Exception;

    private static class XomSourceCallback implements TraxUtils.SourceCallback {

        private Element element;

        public void domSource(Node node) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                element = DOMConverter.convert((org.w3c.dom.Element) node);
            }
            else if (node.getNodeType() == Node.DOCUMENT_NODE) {
                Document document = DOMConverter.convert((org.w3c.dom.Document) node);
                element = document.getRootElement();
            }
            else {
                throw new IllegalArgumentException("DOMSource contains neither Document nor Element");
            }
        }

        public void saxSource(XMLReader reader, InputSource inputSource) throws IOException, SAXException {
            try {
                Builder builder = new Builder(reader);
                Document document;
                if (inputSource.getByteStream() != null) {
                    document = builder.build(inputSource.getByteStream());
                }
                else if (inputSource.getCharacterStream() != null) {
                    document = builder.build(inputSource.getCharacterStream());
                }
                else {
                    throw new IllegalArgumentException(
                            "InputSource in SAXSource contains neither byte stream nor character stream");
                }
                element = document.getRootElement();
            }
            catch (ValidityException ex) {
                throw new XomParsingException(ex);
            }
            catch (ParsingException ex) {
                throw new XomParsingException(ex);
            }
        }

        public void staxSource(XMLEventReader eventReader) throws XMLStreamException {
            throw new IllegalArgumentException("XMLEventReader not supported");
        }

        public void staxSource(XMLStreamReader streamReader) throws XMLStreamException {
            Document document = StaxStreamConverter.convert(streamReader);
            element = document.getRootElement();
        }

        public void streamSource(InputStream inputStream) throws IOException {
            try {
                Builder builder = new Builder();
                Document document = builder.build(inputStream);
                element = document.getRootElement();
            }
            catch (ParsingException ex) {
                throw new XomParsingException(ex);
            }
        }

        public void streamSource(Reader reader) throws IOException {
            try {
                Builder builder = new Builder();
                Document document = builder.build(reader);
                element = document.getRootElement();
            }
            catch (ParsingException ex) {
                throw new XomParsingException(ex);
            }
        }
    }

    private static class XomParsingException extends NestedRuntimeException {

        private XomParsingException(ParsingException ex) {
            super(ex.getMessage(), ex);
        }
    }

    private static class StaxStreamConverter {

        private static Document convert(XMLStreamReader streamReader) throws XMLStreamException {
            NodeFactory nodeFactory = new NodeFactory();
            Document document = null;
            Element element = null;
            ParentNode parent = null;
            boolean documentFinished = false;
            while (streamReader.hasNext()) {
                int event = streamReader.next();
                switch (event) {
                    case XMLStreamConstants.START_DOCUMENT:
                        document = nodeFactory.startMakingDocument();
                        parent = document;
                        break;
                    case XMLStreamConstants.END_DOCUMENT:
                        nodeFactory.finishMakingDocument(document);
                        documentFinished = true;
                        break;
                    case XMLStreamConstants.START_ELEMENT:
                        if (document == null) {
                            document = nodeFactory.startMakingDocument();
                            parent = document;
                        }
                        String name = QNameUtils.toQualifiedName(streamReader.getName());
                        if (element == null) {
                            element = nodeFactory.makeRootElement(name, streamReader.getNamespaceURI());
                            document.setRootElement(element);
                        }
                        else {
                            element = nodeFactory.startMakingElement(name, streamReader.getNamespaceURI());
                            parent.appendChild(element);
                        }
                        convertNamespaces(streamReader, element);
                        convertAttributes(streamReader, nodeFactory);
                        parent = element;
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        nodeFactory.finishMakingElement(element);
                        parent = parent.getParent();
                        break;
                    case XMLStreamConstants.ATTRIBUTE:
                        convertAttributes(streamReader, nodeFactory);
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        nodeFactory.makeText(streamReader.getText());
                        break;
                    case XMLStreamConstants.COMMENT:
                        nodeFactory.makeComment(streamReader.getText());
                        break;
                    default:
                        break;
                }
            }
            if (!documentFinished) {
                nodeFactory.finishMakingDocument(document);
            }
            return document;
        }

        private static void convertNamespaces(XMLStreamReader streamReader, Element element) {
            for (int i = 0; i < streamReader.getNamespaceCount(); i++) {
                String uri = streamReader.getNamespaceURI(i);
                String prefix = streamReader.getNamespacePrefix(i);

                element.addNamespaceDeclaration(prefix, uri);
            }

        }

        private static void convertAttributes(XMLStreamReader streamReader, NodeFactory nodeFactory) {
            for (int i = 0; i < streamReader.getAttributeCount(); i++) {
                String name = QNameUtils.toQualifiedName(streamReader.getAttributeName(i));
                String uri = streamReader.getAttributeNamespace(i);
                String value = streamReader.getAttributeValue(i);
                Attribute.Type type = convertAttributeType(streamReader.getAttributeType(i));

                nodeFactory.makeAttribute(name, uri, value, type);
            }
        }

        private static Attribute.Type convertAttributeType(String type) {
            type = type.toUpperCase(Locale.ENGLISH);
            if ("CDATA".equals(type)) {
                return Attribute.Type.CDATA;
            }
            else if ("ENTITIES".equals(type)) {
                return Attribute.Type.ENTITIES;
            }
            else if ("ENTITY".equals(type)) {
                return Attribute.Type.ENTITY;
            }
            else if ("ENUMERATION".equals(type)) {
                return Attribute.Type.ENUMERATION;
            }
            else if ("ID".equals(type)) {
                return Attribute.Type.ID;
            }
            else if ("IDREF".equals(type)) {
                return Attribute.Type.IDREF;
            }
            else if ("IDREFS".equals(type)) {
                return Attribute.Type.IDREFS;
            }
            else if ("NMTOKEN".equals(type)) {
                return Attribute.Type.NMTOKEN;
            }
            else if ("NMTOKENS".equals(type)) {
                return Attribute.Type.NMTOKENS;
            }
            else if ("NOTATION".equals(type)) {
                return Attribute.Type.NOTATION;
            }
            else {
                return Attribute.Type.UNDECLARED;
            }
        }

    }
}
