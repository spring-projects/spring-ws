/*
 * Copyright 2005-2014 the original author or authors.
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
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * Abstract base class for endpoints that handle the message payload as DOM elements.
 * <p/>
 * Offers the message payload as a DOM <code>Element</code>, and allows subclasses to create a response by returning an
 * <code>Element</code>.
 * <p/>
 * An <code>AbstractDomPayloadEndpoint</code> only accept <em>one</em> payload element. Multiple payload elements are
 * not in accordance with WS-I.
 *
 * @author Arjen Poutsma
 * @author Alef Arendsen
 * @see #invokeInternal(org.w3c.dom.Element,org.w3c.dom.Document)
 * @since 1.0.0
 * @deprecated as of Spring Web Services 2.0, in favor of annotated endpoints
 */
@Deprecated
public abstract class AbstractDomPayloadEndpoint extends TransformerObjectSupport implements PayloadEndpoint {

    private DocumentBuilderFactory documentBuilderFactory;

    private boolean validating = false;

    private boolean namespaceAware = true;

	private boolean expandEntityReferences = false;

    private boolean alwaysTransform = false;

    /** Set whether or not the XML parser should be XML namespace aware. Default is <code>true</code>. */
    public void setNamespaceAware(boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
    }

    /** Set if the XML parser should validate the document. Default is <code>false</code>. */
    public void setValidating(boolean validating) {
        this.validating = validating;
    }

	/**
	 * Set if the XML parser should expand entity reference nodes. Default is
	 * {@code false}.
	 */
	public void setExpandEntityReferences(boolean expandEntityRef) {
		documentBuilderFactory.setExpandEntityReferences(expandEntityRef);
	}


    /**
     * Set if the request {@link Source} should always be transformed into a new {@link DOMResult}.
     * <p/>
     * Default is {@code false}, which is faster.
     */
    public void setAlwaysTransform(boolean alwaysTransform) {
        this.alwaysTransform = alwaysTransform;
    }

    @Override
    public final Source invoke(Source request) throws Exception {
        if (documentBuilderFactory == null) {
            documentBuilderFactory = createDocumentBuilderFactory();
        }
        DocumentBuilder documentBuilder = createDocumentBuilder(documentBuilderFactory);
        Element requestElement = getDocumentElement(request, documentBuilder);
        Document responseDocument = documentBuilder.newDocument();
        Element responseElement = invokeInternal(requestElement, responseDocument);
        return responseElement != null ? new DOMSource(responseElement) : null;
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
	    factory.setExpandEntityReferences(expandEntityReferences);
        return factory;
    }

    /**
     * Returns the payload element of the given source.
     * <p/>
     * Default implementation checks whether the source is a {@link DOMSource}, and returns the {@linkplain
     * DOMSource#getNode() node} of that. In all other cases, or when {@linkplain #setAlwaysTransform(boolean)
     * alwaysTransform} is {@code true}, the source is transformed into a {@link DOMResult}, which is more expensive. If
     * the passed source is {@code null}, {@code null} is returned.
     *
     * @param source          the source to return the root element of; can be {@code null}
     * @param documentBuilder the document builder to be used for transformations
     * @return the document element
     * @throws TransformerException in case of errors
     */
    protected Element getDocumentElement(Source source, DocumentBuilder documentBuilder) throws TransformerException {
        if (source == null) {
            return null;
        }
        if (!alwaysTransform && source instanceof DOMSource) {
            Node node = ((DOMSource) source).getNode();
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) node;
            }
            else if (node.getNodeType() == Node.DOCUMENT_NODE) {
                return ((Document) node).getDocumentElement();
            }
        }
        // we have no other option than to transform
        Document requestDocument = documentBuilder.newDocument();
        DOMResult domResult = new DOMResult(requestDocument);
        transform(source, domResult);
        return requestDocument.getDocumentElement();
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
