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

package org.springframework.ws.server.endpoint.adapter.method;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.springframework.core.MethodParameter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implementation of {@link MethodArgumentResolver} and {@link MethodReturnValueHandler} that supports W3C DOM
 * {@linkplain Element elements}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class DomPayloadMethodProcessor extends AbstractPayloadMethodProcessor {

    // MethodArgumentResolver

    @Override
    protected boolean supportsRequestPayloadParameter(MethodParameter parameter) {
        return supports(parameter);
    }

    @Override
    protected Node resolveRequestPayloadArgument(Source requestPayload, MethodParameter parameter) throws Exception {
        if (requestPayload instanceof DOMSource) {
            return resolveArgumentDomSource(parameter, (DOMSource) requestPayload);
        }
        else {
            DOMResult domResult = new DOMResult();
            transform(requestPayload, domResult);
            DOMSource domSource = new DOMSource(domResult.getNode());
            return resolveArgumentDomSource(parameter, domSource);
        }
    }

    private Node resolveArgumentDomSource(MethodParameter parameter, DOMSource requestSource) {
        Class<?> parameterType = parameter.getParameterType();
        Node requestNode = requestSource.getNode();
        if (parameterType.isAssignableFrom(requestNode.getClass())) {
            return requestNode;
        }
        else if (Element.class.equals(parameterType) && requestNode instanceof Document) {
            Document document = (Document) requestNode;
            return document.getDocumentElement();
        }
        // should not happen
        throw new UnsupportedOperationException();
    }

    // MethodReturnValueHandler

    @Override
    protected boolean supportsResponsePayloadReturnType(MethodParameter returnType) {
        return supports(returnType);
    }

    @Override
    protected DOMSource createResponsePayload(MethodParameter returnType, Object returnValue) {
        Node returnedNode = (Node) returnValue;
        return new DOMSource(returnedNode);
    }

    private boolean supports(MethodParameter parameter) {
        return Element.class.equals(parameter.getParameterType());
    }

}
