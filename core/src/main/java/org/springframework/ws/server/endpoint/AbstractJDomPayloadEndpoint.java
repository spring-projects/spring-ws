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

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.springframework.xml.transform.TransformerObjectSupport;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;
import org.w3c.dom.Node;

/**
 * Abstract base class for endpoints that handle the message payload as JDOM elements.
 * <p/>
 * <p>Offers the message payload as a JDOM {@link Element}, and allows subclasses to create a response by returning an
 * <code>Element</code>.
 * <p/>
 * <pAn <code>AbstractJDomPayloadEndpoint</code> can accept only <i>one</i> payload element. Multiple payload elements
 * are not in accordance with WS-I.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @deprecated as of Spring Web Services 2.0, in favor of annotated endpoints
 */
@Deprecated
public abstract class AbstractJDomPayloadEndpoint extends TransformerObjectSupport implements PayloadEndpoint {

    private boolean alwaysTransform = false;

    /**
     * Set if the request {@link Source} should always be transformed into a new {@link JDOMResult}.
     * <p/>
     * Default is {@code false}, which is faster.
     */
    public void setAlwaysTransform(boolean alwaysTransform) {
        this.alwaysTransform = alwaysTransform;
    }

    public final Source invoke(Source request) throws Exception {
        Element requestElement = getDocumentElement(request);
        Element responseElement = invokeInternal(requestElement);
        return responseElement != null ? new JDOMSource(responseElement) : null;
    }

    /**
     * Returns the payload element of the given source.
     * <p/>
     * Default implementation checks whether the source is a {@link DOMSource}, and uses a {@link DOMBuilder} to create
     * a JDOM {@link Element}. In all other cases, or when {@linkplain #setAlwaysTransform(boolean) alwaysTransform} is
     * {@code true}, the source is transformed into a {@link JDOMResult}, which is more expensive. If the passed source
     * is {@code null}, {@code null} is returned.
     *
     * @param source the source to return the root element of; can be {@code null}
     * @return the document element
     * @throws TransformerException in case of errors
     */
    protected Element getDocumentElement(Source source) throws TransformerException {
        if (source == null) {
            return null;
        }
        if (!alwaysTransform && source instanceof DOMSource) {
            Node node = ((DOMSource) source).getNode();
            DOMBuilder domBuilder = new DOMBuilder();
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return domBuilder.build((org.w3c.dom.Element) node);
            }
            else if (node.getNodeType() == Node.DOCUMENT_NODE) {
                Document document = domBuilder.build((org.w3c.dom.Document) node);
                return document.getRootElement();
            }
        }
        // we have no other option than to transform
        JDOMResult jdomResult = new JDOMResult();
        transform(source, jdomResult);
        return jdomResult.getDocument().getRootElement();
    }

    /**
     * Template method. Subclasses must implement this. Offers the request payload as a JDOM <code>Element</code>, and
     * allows subclasses to return a response <code>Element</code>.
     *
     * @param requestElement the contents of the SOAP message as JDOM element
     * @return the response element. Can be <code>null</code> to specify no response.
     */
    protected abstract Element invokeInternal(Element requestElement) throws Exception;
}
