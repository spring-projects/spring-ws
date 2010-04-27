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

import java.io.ByteArrayInputStream;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.springframework.core.MethodParameter;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Implementation of {@link MethodArgumentResolver} and {@link MethodReturnValueHandler} that supports {@link Source}
 * objects.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class SourcePayloadMethodProcessor extends AbstractPayloadMethodProcessor {

    // MethodArgumentResolver

    @Override
    protected boolean supportsRequestPayloadParameter(MethodParameter parameter) {
        return supports(parameter);
    }

    @Override
    protected Object resolveRequestPayloadArgument(Source requestPayload, MethodParameter parameter) throws Exception {
        Class<?> parameterType = parameter.getParameterType();
        if (parameterType.isAssignableFrom(requestPayload.getClass())) {
            return requestPayload;
        }
        if (DOMSource.class.isAssignableFrom(parameterType)) {
            DOMResult domResult = new DOMResult();
            transform(requestPayload, domResult);
            Node node = domResult.getNode();
            if (node instanceof Document) {
                Document document = (Document) node;
                return new DOMSource(document.getDocumentElement());
            }
            else {
                return new DOMSource(domResult.getNode());
            }
        }
        else if (SAXSource.class.isAssignableFrom(parameterType)) {
            ByteArrayInputStream bis = convertToByteArrayInputStream(requestPayload);
            InputSource inputSource = new InputSource(bis);
            return new SAXSource(inputSource);
        }
        else if (StreamSource.class.isAssignableFrom(parameterType)) {
            ByteArrayInputStream bis = convertToByteArrayInputStream(requestPayload);
            return new StreamSource(bis);
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
    protected Source createResponsePayload(MethodParameter returnType, Object returnValue) {
        return (Source) returnValue;
    }

    private boolean supports(MethodParameter parameter) {
        return Source.class.isAssignableFrom(parameter.getParameterType());
    }

}
