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

package org.springframework.ws.server.endpoint.mapping;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.xml.namespace.QNameUtils;
import org.w3c.dom.Element;

/**
 * Implementation of the <code>EndpointMapping</code> interface to map from the qualified name of the request payload
 * root element. Supports both mapping to bean instances and mapping to bean names: the latter is required for prototype
 * endpoints.
 * <p/>
 * The <code>endpointMap</code> property is suitable for populating the endpoint map with bean references, e.g. via the
 * map element in XML bean definitions.
 * <p/>
 * Mappings to bean names can be set via the <code>mappings</code> property, in a form accepted by the
 * <code>java.util.Properties</code> class, like as follows:
 * <pre>
 * {http://www.springframework.org/spring-ws/samples/airline/schemas}BookFlight=bookFlightEndpoint
 * {http://www.springframework.org/spring-ws/samples/airline/schemas}GetFlights=getFlightsEndpoint
 * </pre>
 * The syntax is QNAME=ENDPOINT_BEAN_NAME. Qualified names are parsed using the syntax described in
 * <code>QNameEditor</code>.
 *
 * @author Arjen Poutsma
 * @see org.springframework.xml.namespace.QNameEditor
 */
public class PayloadRootQNameEndpointMapping extends AbstractQNameEndpointMapping implements InitializingBean {

    private static TransformerFactory transformerFactory;

    protected QName resolveQName(MessageContext messageContext) throws TransformerException {
        Element payloadElement = getMessagePayloadElement(messageContext.getRequest());
        return QNameUtils.getQNameForNode(payloadElement);
    }

    private Element getMessagePayloadElement(WebServiceMessage message) throws TransformerException {
        Transformer transformer = transformerFactory.newTransformer();
        DOMResult domResult = new DOMResult();
        transformer.transform(message.getPayloadSource(), domResult);
        return (Element) domResult.getNode().getFirstChild();
    }

    public final void afterPropertiesSet() throws Exception {
        transformerFactory = TransformerFactory.newInstance();
    }
}
