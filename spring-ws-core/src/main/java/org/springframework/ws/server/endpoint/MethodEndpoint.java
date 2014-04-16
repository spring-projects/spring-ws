/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Represents a bean method that will be invoked as part of an incoming Web service message.
 *
 * <p>Consists of a {@link Method}, and a bean {@link Object}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public final class MethodEndpoint {

    private final Object bean;

    private final Method method;

    private final BeanFactory beanFactory;

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
        this.beanFactory = null;
    }

    /**
     * Constructs a new method endpoint with the given bean, method name and parameters.
     *
     * @param bean           the object bean
     * @param methodName     the method name
     * @param parameterTypes the method parameter types
     * @throws NoSuchMethodException when the method cannot be found
     */
    public MethodEndpoint(Object bean, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Assert.notNull(bean, "bean must not be null");
        Assert.notNull(methodName, "method must not be null");
        this.bean = bean;
        this.method = bean.getClass().getMethod(methodName, parameterTypes);
        this.beanFactory = null;
    }

    /**
     * Constructs a new method endpoint with the given bean name and method. The bean name will be lazily initialized when
     * {@link #invoke(Object...)} is called.
     *
     * @param beanName    the bean name
     * @param beanFactory the bean factory to use for bean initialization
     * @param method      the method
     */
    public MethodEndpoint(String beanName, BeanFactory beanFactory, Method method) {
        Assert.hasText(beanName, "'beanName' must not be null");
        Assert.notNull(beanFactory, "'beanFactory' must not be null");
        Assert.notNull(method, "'method' must not be null");
        Assert.isTrue(beanFactory.containsBean(beanName),
                "Bean factory [" + beanFactory + "] does not contain bean " + "with name [" + beanName + "]");
        this.bean = beanName;
        this.beanFactory = beanFactory;
        this.method = method;
    }

    /** Returns the object bean for this method endpoint. */
    public Object getBean() {
        if (beanFactory != null && bean instanceof String) {
            String beanName = (String) bean;
            return beanFactory.getBean(beanName);
        }
        else {
            return bean;
        }
    }

    /** Returns the method for this method endpoint. */
    public Method getMethod() {
        return this.method;
    }

    /** Returns the method parameters for this method endpoint. */
    public MethodParameter[] getMethodParameters() {
        int parameterCount = getMethod().getParameterTypes().length;
        MethodParameter[] parameters = new MethodParameter[parameterCount];
        for (int i = 0; i < parameterCount; i++) {
            parameters[i] = new MethodParameter(getMethod(), i);
        }
        return parameters;
    }

    /** Returns the method return type, as {@code MethodParameter}. */
    public MethodParameter getReturnType() {
        return new MethodParameter(method, -1);
    }

    /**
     * Invokes this method endpoint with the given arguments.
     *
     * @param args the arguments
     * @return the invocation result
     * @throws Exception when the method invocation results in an exception
     */
    public Object invoke(Object... args) throws Exception {
        Object endpoint = getBean();
        ReflectionUtils.makeAccessible(method);
        try {
            return method.invoke(endpoint, args);
        }
        catch (InvocationTargetException ex) {
            handleInvocationTargetException(ex);
            throw new IllegalStateException(
                    "Unexpected exception thrown by method - " + ex.getTargetException().getClass().getName() + ": " +
                            ex.getTargetException().getMessage());
        }
    }

    private void handleInvocationTargetException(InvocationTargetException ex) throws Exception {
        Throwable targetException = ex.getTargetException();
        if (targetException instanceof RuntimeException) {
            throw (RuntimeException) targetException;
        }
        if (targetException instanceof Error) {
            throw (Error) targetException;
        }
        if (targetException instanceof Exception) {
            throw (Exception) targetException;
        }

    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof MethodEndpoint) {
            MethodEndpoint other = (MethodEndpoint) o;
            return this.bean.equals(other.bean) && this.method.equals(other.method);
        }
        return false;
    }

    public int hashCode() {
        return 31 * this.bean.hashCode() + this.method.hashCode();
    }

    public String toString() {
        return method.toGenericString();
    }

}
