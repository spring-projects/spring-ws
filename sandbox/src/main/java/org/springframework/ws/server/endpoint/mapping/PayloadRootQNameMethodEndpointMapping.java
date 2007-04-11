/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.ws.server.endpoint.mapping;

import java.lang.reflect.Method;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.PayloadRootQName;
import org.springframework.xml.namespace.QNameUtils;
import org.w3c.dom.Element;

/** @author Arjen Poutsma */
public class PayloadRootQNameMethodEndpointMapping extends AbstractAnnotationEndpointMapping {

    private static TransformerFactory transformerFactory;

    static {
        transformerFactory = TransformerFactory.newInstance();
    }

    protected String getLookupKeyForMessage(MessageContext messageContext) throws Exception {
        Element payloadElement = getMessagePayloadElement(messageContext.getRequest());
        QName qName = QNameUtils.getQNameForNode(payloadElement);
        return qName != null ? qName.toString() : null;
    }

    protected String getLookupKeyForMethod(Method method) {
        PayloadRootQName annotation = AnnotationUtils.getAnnotation(method, PayloadRootQName.class);
        return annotation != null ? annotation.value() : null;
    }

    private Element getMessagePayloadElement(WebServiceMessage message) throws TransformerException {
        Transformer transformer = transformerFactory.newTransformer();
        DOMResult domResult = new DOMResult();
        transformer.transform(message.getPayloadSource(), domResult);
        return (Element) domResult.getNode().getFirstChild();
    }


}
