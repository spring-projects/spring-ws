/*
 * Copyright 2005 the original author or authors.
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.springframework.xml.transform.TransformerObjectSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Abstract base class for endpoints that handle the message payload as DOM elements.
 * <p/>
 * <p>Offers the message payload as a DOM <code>Element</code>, and allows subclasses to create a response by returning
 * an <code>Element</code>.
 * <p/>
 * <p>An <code>AbstractDomPayloadEndpoint</code> only accept <i>one</i> payload element. Multiple payload elements are
 * not in accordance with WS-I.
 *
 * @author Arjen Poutsma
 * @author Alef Arendsen
 * @see #invokeInternal(org.w3c.dom.Element,org.w3c.dom.Document)
 */
public abstract class AbstractDomPayloadEndpoint extends TransformerObjectSupport implements PayloadEndpoint {

    private DocumentBuilderFactory documentBuilderFactory;

    private boolean validating = false;

    private boolean namespaceAware = true;

    /** Set whether or not the XML parser should be XML namespace aware. Default is <code>true</code>. */
    public void setNamespaceAware(boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
    }

    /** Set if the XML parser should validate the document. Default is <code>false</code>. */
    public void setValidating(boolean validating) {
        this.validating = validating;
    }

    public final Source invoke(Source request) throws Exception {
        if (documentBuilderFactory == null) {
            documentBuilderFactory = createDocumentBuilderFactory();
        }
        DocumentBuilder documentBuilder = createDocumentBuilder(documentBuilderFactory);
        Document requestDocument = documentBuilder.newDocument();
        DOMResult domResult = new DOMResult(requestDocument);
        transform(request, domResult);
        Element requestElement = (Element) requestDocument.getFirstChild();
        Document responseDocument = documentBuilder.newDocument();
        Element responseElement = invokeInternal(requestElement, responseDocument);
        if (responseElement != null) {
            return new DOMSource(responseElement);
        }
        else {
            return null;
        }
    }

    /**
     * Create a <code>DocumentBuilder</code> that this endpoint will use for parsing XML documents. Can be overridden in
     * subclasses, adding further initialization of the builder.
     *
     * @param factory the <code>DocumentBuilderFactory</code> that the DocumentBuilder should be created with
     * @return the <code>DocumentBuilder</code>
     * @throws ParserConfigurationException if thrown by JAXP methods
     */
    protected DocumentBuilder createDocumentBuilder(DocumentBuilderFactory factory)
            throws ParserConfigurationException {
        return factory.newDocumentBuilder();
    }

    /**
     * Create a <code>DocumentBuilderFactory</code> that this endpoint will use for constructing XML documents. Can be
     * overridden in subclasses, adding further initialization of the factory. The resulting
     * <code>DocumentBuilderFactory</code> is cached, so this method will only be called once.
     *
     * @return the DocumentBuilderFactory
     * @throws ParserConfigurationException if thrown by JAXP methods
     */
    protected DocumentBuilderFactory createDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(validating);
        factory.setNamespaceAware(namespaceAware);
        return factory;
    }

    /**
     * Template method that subclasses must implement to process the request.
     * <p/>
     * <p>Offers the request payload as a DOM <code>Element</code>, and allows subclasses to return a response
     * <code>Element</code>.
     * <p/>
     * <p>The given DOM <code>Document</code> is to be used for constructing <code>Node</code>s, by using the various
     * <code>create</code> methods.
     *
     * @param requestElement   the contents of the SOAP message as DOM elements
     * @param responseDocument a DOM document to be used for constructing <code>Node</code>s
     * @return the response element. Can be <code>null</code> to specify no response.
     */
    protected abstract Element invokeInternal(Element requestElement, Document responseDocument) throws Exception;
}
