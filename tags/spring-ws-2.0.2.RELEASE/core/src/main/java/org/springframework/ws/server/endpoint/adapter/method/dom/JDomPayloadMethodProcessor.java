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
import javax.xml.transform.dom.DOMSource;

import org.springframework.core.MethodParameter;
import org.springframework.ws.server.endpoint.adapter.method.AbstractPayloadSourceMethodProcessor;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;
import org.w3c.dom.Node;

/**
 * Implementation of {@link org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver
 * MethodArgumentResolver} and {@link org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler
 * MethodReturnValueHandler} that supports JDOM {@linkplain Element elements}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class JDomPayloadMethodProcessor extends AbstractPayloadSourceMethodProcessor {

    @Override
    protected boolean supportsRequestPayloadParameter(MethodParameter parameter) {
        return supports(parameter);
    }

    @Override
    protected Element resolveRequestPayloadArgument(MethodParameter parameter, Source requestPayload) throws Exception {
        if (requestPayload instanceof DOMSource) {
            Node node = ((DOMSource) requestPayload).getNode();
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
        transform(requestPayload, jdomResult);
        return jdomResult.getDocument().getRootElement();
    }

    @Override
    protected boolean supportsResponsePayloadReturnType(MethodParameter returnType) {
        return supports(returnType);
    }

    @Override
    protected Source createResponsePayload(MethodParameter returnType, Object returnValue) {
        Element returnedElement = (Element) returnValue;
        return new JDOMSource(returnedElement);
    }

    private boolean supports(MethodParameter parameter) {
        return Element.class.equals(parameter.getParameterType());
    }

}