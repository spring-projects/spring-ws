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
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.support.QNameUtils;
import org.w3c.dom.Element;

/**
 * Endpoint mapping that returns resolves the qualified name of the first element of the message payload.
 *
 * @author Arjen Poutsma
 */
public class PayloadRootQNameEndpointMapping extends AbstractQNameEndpointMapping implements InitializingBean {

    private Transformer transformer;

    protected QName resolveQName(WebServiceMessage message) throws TransformerException {
        Element payloadElement = getMessagePayloadElement(message);
        return QNameUtils.getQNameForNode(payloadElement);
    }

    private Element getMessagePayloadElement(WebServiceMessage message) throws TransformerException {
        DOMResult domResult = new DOMResult();
        transformer.transform(message.getPayloadSource(), domResult);
        return (Element) domResult.getNode().getFirstChild();
    }

    public void afterPropertiesSet() throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
    }
}
