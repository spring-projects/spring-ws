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

package org.springframework.ws.server.endpoint;

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.converters.DOMConverter;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import org.springframework.xml.transform.TransformerObjectSupport;

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
 */
public abstract class AbstractXomPayloadEndpoint extends TransformerObjectSupport implements PayloadEndpoint {

    private DocumentBuilderFactory documentBuilderFactory;

    public final Source invoke(Source request) throws Exception {
        Element requestElement = null;
        if (request != null) {
            if (request instanceof DOMSource) {
                requestElement = handleDomSource((DOMSource) request);
            }
            else if (request instanceof SAXSource) {
                requestElement = handleSaxSource((SAXSource) request);
            }
            else if (request instanceof StreamSource) {
                requestElement = handleStreamSource((StreamSource) request);
            }
            else {
                throw new IllegalArgumentException("Source [" + request.getClass().getName() +
                        "] is neither SAXSource, DOMSource, nor StreamSource");
            }
        }
        Element responseElement = invokeInternal(requestElement);
        Source result;
        if (responseElement != null) {
            if (documentBuilderFactory == null) {
                createDocumentBuilderFactory();
            }
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document responseDocument = new Document(responseElement);
            org.w3c.dom.Document w3cDocument =
                    DOMConverter.convert(responseDocument, documentBuilder.getDOMImplementation());
            return new DOMSource(w3cDocument);
        }
        else {
            result = null;
        }
        return result;
    }

    private Element handleDomSource(DOMSource request) {
        Node w3cNode = request.getNode();
        org.w3c.dom.Element w3cElement = null;
        if (w3cNode.getNodeType() == Node.ELEMENT_NODE) {
            w3cElement = (org.w3c.dom.Element) w3cNode;
        }
        else if (w3cNode.getNodeType() == Node.DOCUMENT_NODE) {
            org.w3c.dom.Document w3cDocument = (org.w3c.dom.Document) w3cNode;
            w3cElement = w3cDocument.getDocumentElement();
        }
        return DOMConverter.convert(w3cElement);
    }

    private Element handleSaxSource(SAXSource request) throws ParsingException, IOException {
        Builder builder = new Builder(request.getXMLReader());
        InputSource inputSource = request.getInputSource();
        Document document;
        if (inputSource.getByteStream() != null) {
            document = builder.build(inputSource.getByteStream());
        }
        else if (inputSource.getCharacterStream() != null) {
            document = builder.build(inputSource.getCharacterStream());
        }
        else {
            throw new IllegalArgumentException(
                    "InputSource in SAXSource contains neither byte stream nor " + "character stream");
        }
        return document.getRootElement();
    }

    private Element handleStreamSource(StreamSource request) throws ParsingException, IOException {
        Builder builder = new Builder();
        Document document;
        if (request.getInputStream() != null) {
            document = builder.build(request.getInputStream());
        }
        else if (request.getReader() != null) {
            document = builder.build(request.getReader());
        }
        else {
            throw new IllegalArgumentException("StreamSource contains neither byte stream nor character stream");
        }
        return document.getRootElement();
    }

    private void createDocumentBuilderFactory() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
    }

    /**
     * Template method. Subclasses must implement this. Offers the request payload as a XOM <code>Element</code>, and
     * allows subclasses to return a response <code>Element</code>.
     *
     * @param requestElement the contents of the SOAP message as XOM element
     * @return the response element. Can be <code>null</code> to specify no response.
     */
    protected abstract Element invokeInternal(Element requestElement) throws Exception;


}
