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

import javax.xml.transform.Source;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * Abstract base class for endpoints that handle the message payload as dom4j elements. Offers the message payload as a
 * dom4j <code>Element</code>, and allows subclasses to create a response by returning an <code>Element</code>.
 * <p/>
 * An <code>AbstractDom4JPayloadEndpoint</code> only accept one payload element. Multiple payload elements are not in
 * accordance with WS-I.
 *
 * @author Arjen Poutsma
 * @see org.dom4j.Element
 * @since 1.0.0
 */
public abstract class AbstractDom4jPayloadEndpoint extends TransformerObjectSupport implements PayloadEndpoint {

    public final Source invoke(Source request) throws Exception {
        Element requestElement = null;
        if (request != null) {
            DocumentResult dom4jResult = new DocumentResult();
            transform(request, dom4jResult);
            requestElement = dom4jResult.getDocument().getRootElement();
        }
        Document responseDocument = DocumentHelper.createDocument();
        Element responseElement = invokeInternal(requestElement, responseDocument);
        return responseElement != null ? new DocumentSource(responseElement) : null;
    }

    /**
     * Template method. Subclasses must implement this. Offers the request payload as a dom4j <code>Element</code>, and
     * allows subclasses to return a response <code>Element</code>.
     * <p/>
     * The given dom4j <code>Document</code> is to be used for constructing a response element, by using
     * <code>addElement</code>.
     *
     * @param requestElement   the contents of the SOAP message as dom4j elements
     * @param responseDocument a dom4j document to be used for constructing a response
     * @return the response element. Can be <code>null</code> to specify no response.
     */
    protected abstract Element invokeInternal(Element requestElement, Document responseDocument) throws Exception;
}
