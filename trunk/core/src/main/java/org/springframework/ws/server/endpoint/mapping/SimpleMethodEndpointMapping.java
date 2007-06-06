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
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.support.PayloadRootUtils;

/**
 * Simple subclass of {@link AbstractMethodEndpointMapping} that maps from the local name of the request payload to
 * methods.Endpoint beans are registered using the <code>endpoints</code> property; the endpoint methods that start with
 * <code>methodPrefix</code> and end with <code>methodSuffix</code> will be registered.
 * <p/>
 * Endpoints typically have the following form:
 * <pre>
 * public class MyEndpoint{
 * <p/>
 *    public Source handleMyMessage(Source source) {
 *       ...
 *    }
 * }
 * </pre>
 * This method will handle any message that has the <code>MyMessage</code> as a payload root local name.
 *
 * @author Arjen Poutsma
 * @see #setEndpoints(Object[])
 */
public class SimpleMethodEndpointMapping extends AbstractMethodEndpointMapping implements InitializingBean {

    /** Default method prefix. */
    public static final String DEFAULT_METHOD_PREFIX = "handle";

    /** Default method suffix. */
    public static final String DEFAULT_METHOD_SUFFIX = "";

    private Object[] endpoints;

    private String methodPrefix = DEFAULT_METHOD_PREFIX;

    private String methodSuffix = DEFAULT_METHOD_SUFFIX;

    private TransformerFactory transformerFactory;

    public Object[] getEndpoints() {
        return endpoints;
    }

    /**
     * Sets the endpoints. The endpoint methods that start with <code>methodPrefix</code> and end with
     * <code>methodSuffix</code> will be registered.
     */
    public void setEndpoints(Object[] endpoints) {
        this.endpoints = endpoints;
    }

    /** Returns the method prefix. */
    public String getMethodPrefix() {
        return methodPrefix;
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

    /** Returns the method suffix. */
    public String getMethodSuffix() {
        return methodSuffix;
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

    public final void afterPropertiesSet() throws Exception {
        Assert.notEmpty(getEndpoints(), "'endpoints' is required");
        transformerFactory = TransformerFactory.newInstance();
        for (int i = 0; i < getEndpoints().length; i++) {
            registerMethods(getEndpoints()[i]);
        }
    }

    /** Returns the name of the given method, with the prefix and suffix stripped off. */
    protected String getLookupKeyForMethod(Method method) {
        String methodName = method.getName();
        String prefix = getMethodPrefix();
        String suffix = getMethodSuffix();
        if (methodName.startsWith(prefix) && methodName.endsWith(suffix)) {
            return methodName.substring(prefix.length(), methodName.length() - suffix.length());
        }
        else {
            return null;
        }
    }

    /** Returns the local part of the payload root element of the request. */
    protected String getLookupKeyForMessage(MessageContext messageContext) throws TransformerException {
        WebServiceMessage request = messageContext.getRequest();
        QName rootQName = PayloadRootUtils.getPayloadRootQName(request.getPayloadSource(), transformerFactory);
        return rootQName.getLocalPart();
    }
}
