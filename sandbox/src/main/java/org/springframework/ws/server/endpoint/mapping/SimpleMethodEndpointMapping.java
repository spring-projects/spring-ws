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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Simple subclass of {@link AbstractMethodEndpointMapping} that maps to all methods that start with a prefix, and end
 * with a suffix. Endpoint beans are registered using the <code>endpoints</code> property.
 *
 * @author Arjen Poutsma
 */
public class SimpleMethodEndpointMapping extends AbstractMethodEndpointMapping implements InitializingBean {

    public static final String DEFAULT_METHOD_PREFIX = "handle";

    public static final String DEFAULT_METHOD_SUFFIX = "";

    private Object[] endpoints;

    private String methodPrefix = DEFAULT_METHOD_PREFIX;

    private String methodSuffix = DEFAULT_METHOD_SUFFIX;

    private TransformerFactory transformerFactory;

    /** Sets the endpoints */
    public void setEndpoints(Object[] endpoints) {
        this.endpoints = endpoints;
    }

    /**
     * Sets the method prefix. All methods with names starting with this string will be registered. Default is
     * "<code>handle</code>".
     *
     * @see #DEFAULT_METHOD_PREFIX
     */
    public void setMethodPrefix(String methodPrefix) {
        this.methodPrefix = methodPrefix;
    }

    /**
     * Sets the method suffix. All methods with names ending with this string will be registered. Default is "" (i.e. no
     * suffix).
     *
     * @see #DEFAULT_METHOD_SUFFIX
     */
    public void setMethodSuffix(String methodSuffix) {
        this.methodSuffix = methodSuffix;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notEmpty(endpoints, "endpoints is required");
        transformerFactory = TransformerFactory.newInstance();
        for (int i = 0; i < endpoints.length; i++) {
            registerMethods(endpoints[i]);
        }
    }

    /** Returns the name of the given method, with the prefix and suffix stripped off. */
    protected String getLookupKeyForMethod(Method method) {
        String methodName = method.getName();
        if (methodName.startsWith(methodPrefix) && methodName.endsWith(methodSuffix)) {
            return methodName.substring(methodPrefix.length(), methodName.length() - methodSuffix.length());
        }
        else {
            return null;
        }
    }

    protected String getLookupKeyForMessage(MessageContext messageContext) throws TransformerException {
        Element payloadElement = getMessagePayloadElement(messageContext.getRequest());
        return payloadElement.getLocalName();
    }

    private Element getMessagePayloadElement(WebServiceMessage message) throws TransformerException {
        if (message.getPayloadSource() instanceof DOMSource) {
            DOMSource domSource = (DOMSource) message.getPayloadSource();
            if (domSource.getNode().getNodeType() == Node.ELEMENT_NODE) {
                return (Element) domSource.getNode();
            }
        }
        Transformer transformer = transformerFactory.newTransformer();
        DOMResult domResult = new DOMResult();
        transformer.transform(message.getPayloadSource(), domResult);
        return (Element) domResult.getNode().getFirstChild();
    }


}
