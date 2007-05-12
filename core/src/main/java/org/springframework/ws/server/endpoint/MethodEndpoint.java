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

package org.springframework.ws.server.endpoint;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Represents a bean method that will be invoked as part of an incoming Web service message.
 * <p/>
 * Consists of a {@link Method}, and a bean {@link Object}.
 *
 * @author Arjen Poutsma
 */
public final class MethodEndpoint {

    private Object bean;

    private Method method;

    /**
     * Constructs a new method endpoint with the given bean and method.
     *
     * @param bean   the object bean
     * @param method the method
     */
    public MethodEndpoint(Object bean, Method method) {
        Assert.notNull(bean, "bean must not be null");
        Assert.notNull(method, "method must not be null");
        this.bean = bean;
        this.method = method;
    }

    /**
     * Constructs a new method endpoint with the given bean, method name and parameters.
     *
     * @param bean           the object bean
     * @param methodName     the method name
     * @param parameterTypes the method parameter types
     */
    public MethodEndpoint(Object bean, String methodName, Class[] parameterTypes) throws NoSuchMethodException {
        Assert.notNull(bean, "bean must not be null");
        Assert.notNull(methodName, "method must not be null");
        this.bean = bean;
        method = bean.getClass().getMethod(methodName, parameterTypes);
    }

    /** Returns the object bean for this method endpoint. */
    public Object getBean() {
        return bean;
    }

    /** Returns the method for this method endpoint. */
    public Method getMethod() {
        return method;
    }

    /**
     * Invokes this method endpoint with the given arguments.
     *
     * @param args the arguments
     * @return the invocation result
     * @throws IllegalAccessException    when there is insufficient access to invoke the method
     * @throws InvocationTargetException when the method invocation results in an exception
     */
    public Object invoke(Object[] args) throws IllegalAccessException, InvocationTargetException {
        return ReflectionUtils.invokeMethod(method, bean, args);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof MethodEndpoint) {
            MethodEndpoint other = (MethodEndpoint) o;
            return bean.equals(other.bean) && method.equals(other.method);
        }
        return false;
    }

    public int hashCode() {
        return 31 * bean.hashCode() + method.hashCode();
    }

    public String toString() {
        return method.toString();
    }
}
