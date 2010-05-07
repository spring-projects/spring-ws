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

package org.springframework.ws.server.endpoint.adapter.method.dom;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.springframework.core.MethodParameter;
import org.springframework.ws.server.endpoint.adapter.method.AbstractPayloadSourceMethodProcessor;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;

/**
 * Implementation of {@link org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver} and {@link org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler} that supports dom4j
 * {@linkplain Element elements}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class Dom4jPayloadMethodProcessor extends AbstractPayloadSourceMethodProcessor {

    @Override
    protected boolean supportsRequestPayloadParameter(MethodParameter parameter) {
        return supports(parameter);
    }

    @Override
    protected Element resolveRequestPayloadArgument(MethodParameter parameter, Source requestPayload)
            throws TransformerException {
        if (requestPayload instanceof DOMSource) {
            org.w3c.dom.Node node = ((DOMSource) requestPayload).getNode();
            if (node.getNodeType() == org.w3c.dom.Node.DOCUMENT_NODE) {
                DOMReader domReader = new DOMReader();
                Document document = domReader.read((org.w3c.dom.Document) node);
                return document.getRootElement();
            }
        }
        // we have no other option than to transform
        DocumentResult dom4jResult = new DocumentResult();
        transform(requestPayload, dom4jResult);
        return dom4jResult.getDocument().getRootElement();
    }

    @Override
    protected boolean supportsResponsePayloadReturnType(MethodParameter returnType) {
        return supports(returnType);
    }

    @Override
    protected Source createResponsePayload(MethodParameter returnType, Object returnValue) {
        Element returnedElement = (Element) returnValue;
        return new DocumentSource(returnedElement);
    }

    private boolean supports(MethodParameter parameter) {
        return Element.class.equals(parameter.getParameterType());
    }

}