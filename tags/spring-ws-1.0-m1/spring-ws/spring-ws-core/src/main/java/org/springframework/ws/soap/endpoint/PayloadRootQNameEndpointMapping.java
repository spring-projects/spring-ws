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

package org.springframework.ws.soap.endpoint;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Element;

import org.springframework.ws.WebServiceMessage;
import org.springframework.xml.namespace.QNameUtils;

/**
 * Endpoint mapping that returns resolves the qualified name of the first element of the message payload.
 *
 * @author Arjen Poutsma
 */
public class PayloadRootQNameEndpointMapping extends AbstractQNameEndpointMapping {

    private static TransformerFactory transformerFactory;

    protected QName resolveQName(WebServiceMessage message) throws TransformerException {
        Element payloadElement = getMessagePayloadElement(message);
        return QNameUtils.getQNameForNode(payloadElement);
    }

    private Element getMessagePayloadElement(WebServiceMessage message) throws TransformerException {
        Transformer transformer = createTransformer();
        DOMResult domResult = new DOMResult();
        transformer.transform(message.getPayloadSource(), domResult);
        return (Element) domResult.getNode().getFirstChild();
    }

    private Transformer createTransformer() throws TransformerConfigurationException {
        if (transformerFactory == null) {
            transformerFactory = TransformerFactory.newInstance();
        }
        return transformerFactory.newTransformer();
    }

}
