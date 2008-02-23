/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.soap.addressing;

import java.lang.reflect.Method;
import java.net.URI;

import org.springframework.aop.support.AopUtils;
import org.springframework.core.JdkVersion;
import org.springframework.util.Assert;
import org.springframework.ws.server.endpoint.MethodEndpoint;

/**
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public abstract class AbstractActionMethodEndpointMapping extends AbstractActionEndpointMapping {

    /**
     * Helper method that registers the methods of the given bean. This method iterates over the methods of the bean,
     * and calls {@link #getActionForMethod(Method)} for each. If this returns a URI, the method is registered using
     * {@link #registerEndpoint(URI, Object)}.
     *
     * @see #getActionForMethod (java.lang.reflect.Method)
     */
    protected void registerMethods(Object endpoint) {
        Assert.notNull(endpoint, "'endpoint' must not be null");
        Method[] methods = getEndpointClass(endpoint).getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (JdkVersion.isAtLeastJava15() && methods[i].isSynthetic() ||
                    methods[i].getDeclaringClass().equals(Object.class)) {
                continue;
            }
            URI action = getActionForMethod(methods[i]);
            if (action != null) {
                registerEndpoint(action, new MethodEndpoint(endpoint, methods[i]));
            }
        }
    }

    /**
     * Returns the the action URI for the given method. Returns <code>null</code> if the method is not to be registered,
     * which is the default.
     *
     * @param method the method
     * @return the action URI, or <code>null</code> if the method is not to be registered
     */
    protected URI getActionForMethod(Method method) {
        return null;
    }

    /**
     * Return the class or interface to use for method reflection.
     * <p/>
     * Default implementation delegates to {@link AopUtils#getTargetClass(Object)}.
     *
     * @param endpoint the bean instance (might be an AOP proxy)
     * @return the bean class to expose
     */
    protected Class getEndpointClass(Object endpoint) {
        return AopUtils.getTargetClass(endpoint);
    }

}
