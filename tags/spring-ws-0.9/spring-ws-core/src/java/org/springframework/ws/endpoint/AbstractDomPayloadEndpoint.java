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

package org.springframework.ws.endpoint;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Abstract base class for endpoints that handle the message payload as DOM elements. Offers the message payload as a
 * DOM <code>Element</code>, and allows subclasses to create a response by returning an <code>Elements</code>.
 * <p/>
 * An <code>AbstractDomPayloadEndpoint</code> only accept one payload element. Multiple payload elements are not in
 * accordance with WS-I.
 *
 * @author Arjen Poutsma
 * @author Alef Arendsen
 * @see #invokeInternal(org.w3c.dom.Element, org.w3c.dom.Document)
 */
public abstract class AbstractDomPayloadEndpoint implements PayloadEndpoint, InitializingBean {

    protected final Log logger = LogFactory.getLog(getClass());

    private Transformer transformer;

    private DocumentBuilder documentBuilder;

    public final void afterPropertiesSet() throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        onAfterPropertiesSet();
    }

    public final Source invoke(Source request) throws Exception {
        DOMResult domResult = new DOMResult();
        transformer.transform(request, domResult);
        Element requestElement = (Element) ((Document) domResult.getNode()).getFirstChild();
        Document document = documentBuilder.newDocument();
        Element responseElement = invokeInternal(requestElement, document);
        if (responseElement != null) {
            document.normalizeDocument();
            return new DOMSource(responseElement);
        }
        else {
            return null;
        }
    }

    /**
     * Template method which can be used for initalization.
     */
    protected void onAfterPropertiesSet() throws Exception {
    }

    /**
     * Template method. Subclasses must implement this. Offers the request payload as a DOM <code>Element</code>, and
     * allows subclasses to return a response <code>Element</code>.
     * <p/>
     * The given DOM <code>Document</code> is to be used for constructing <code>Node</code>s, by using the various
     * <code>create</code> methods.
     *
     * @param requestElement the contents of the SOAP message as DOM elements
     * @param document       a DOM document to be used for constructing <code>Node</code>s
     * @return the response element. Can be <code>null</code> to specify no response.
     */
    protected abstract Element invokeInternal(Element requestElement, Document document) throws Exception;
}
