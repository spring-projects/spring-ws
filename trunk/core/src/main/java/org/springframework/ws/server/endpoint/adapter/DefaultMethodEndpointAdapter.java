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

package org.springframework.ws.server.endpoint.adapter;

import java.util.Arrays;
import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;
import org.springframework.ws.support.DefaultStrategiesHelper;

/**
 * Default extension of {@link AbstractMethodEndpointAdapter} with support for pluggable {@linkplain
 * MethodArgumentResolver argument resolvers} and {@linkplain MethodReturnValueHandler return value handlers}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class DefaultMethodEndpointAdapter extends AbstractMethodEndpointAdapter {

    private List<MethodArgumentResolver> methodArgumentResolvers;

    private List<MethodReturnValueHandler> methodReturnValueHandlers;

    /**
     * Initializes a {@code DefaultMethodEndpointAdapter} with the default strategies.
     *
     * @see #initDefaultStrategies()
     */
    public DefaultMethodEndpointAdapter() {
        initDefaultStrategies();
    }

    /** Sets the list of {@code MethodArgumentResolver}s to use. */
    public void setMethodArgumentResolvers(List<MethodArgumentResolver> methodArgumentResolvers) {
        this.methodArgumentResolvers = methodArgumentResolvers;
    }

    /** Sets the list of {@code MethodReturnValueHandler}s to use. */
    public void setMethodReturnValueHandlers(List<MethodReturnValueHandler> methodReturnValueHandlers) {
        this.methodReturnValueHandlers = methodReturnValueHandlers;
    }

    /** Initialize the default implementations for the adapter's strategies */
    protected void initDefaultStrategies() {
        Resource resource =
                new ClassPathResource(ClassUtils.getShortName(DefaultMethodEndpointAdapter.class) + ".properties",
                        DefaultMethodEndpointAdapter.class);
        DefaultStrategiesHelper strategiesHelper = new DefaultStrategiesHelper(resource);
        if (CollectionUtils.isEmpty(methodArgumentResolvers)) {
            List<MethodArgumentResolver> methodArgumentResolvers =
                    strategiesHelper.getDefaultStrategies(MethodArgumentResolver.class);
            setMethodArgumentResolvers(methodArgumentResolvers);
        }
        if (CollectionUtils.isEmpty(methodReturnValueHandlers)) {
            List<MethodReturnValueHandler> methodReturnValueHandlers =
                    strategiesHelper.getDefaultStrategies(MethodReturnValueHandler.class);
            setMethodReturnValueHandlers(methodReturnValueHandlers);
        }
    }

    @Override
    protected boolean supportsInternal(MethodEndpoint methodEndpoint) {
        return supportsParameters(methodEndpoint.getMethodParameters()) &&
                supportsReturnType(methodEndpoint.getReturnType());
    }

    private boolean supportsParameters(MethodParameter[] methodParameters) {
        for (MethodParameter methodParameter : methodParameters) {
            boolean supported = false;
            for (MethodArgumentResolver methodArgumentResolver : methodArgumentResolvers) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Testing if endpoint adapter [" + methodArgumentResolver + "] supports [" +
                            methodParameter.getGenericParameterType() + "]");
                }
                if (methodArgumentResolver.supportsParameter(methodParameter)) {
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                return false;
            }
        }
        return true;
    }

    private boolean supportsReturnType(MethodParameter methodReturnType) {
        if (Void.TYPE.equals(methodReturnType.getParameterType())) {
            return true;
        }
        for (MethodReturnValueHandler methodReturnValueHandler : methodReturnValueHandlers) {
            if (methodReturnValueHandler.supportsReturnType(methodReturnType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected final void invokeInternal(MessageContext messageContext, MethodEndpoint methodEndpoint) throws Exception {
        Object[] args = getMethodArguments(messageContext, methodEndpoint);

        if (logger.isTraceEnabled()) {
            StringBuilder builder = new StringBuilder("Invoking [");
            builder.append(methodEndpoint).append("] with arguments ");
            builder.append(Arrays.asList(args));
            logger.trace(builder.toString());
        }

        Object returnValue = methodEndpoint.invoke(args);

        if (logger.isTraceEnabled()) {
            logger.trace("Method [" + methodEndpoint + "] returned [" + returnValue + "]");
        }

        Class<?> returnType = methodEndpoint.getMethod().getReturnType();
        if (!Void.TYPE.equals(returnType)) {
            handleMethodReturnValue(messageContext, returnValue, methodEndpoint);
        }
    }

    /**
     * Returns the argument array for the given method endpoint.
     * <p/>
     * This implementation iterates over the set {@linkplain #setMethodArgumentResolvers(List) argument resolvers} to
     * resolve each argument.
     *
     * @param messageContext the current message context
     * @param methodEndpoint the method endpoint to get arguments for
     * @return the arguments
     * @throws Exception in case of errors
     */
    protected Object[] getMethodArguments(MessageContext messageContext, MethodEndpoint methodEndpoint)
            throws Exception {
        MethodParameter[] parameters = methodEndpoint.getMethodParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            for (MethodArgumentResolver methodArgumentResolver : methodArgumentResolvers) {
                if (methodArgumentResolver.supportsParameter(parameters[i])) {
                    args[i] = methodArgumentResolver.resolveArgument(messageContext, parameters[i]);
                    break;
                }
            }
        }
        return args;
    }

    /**
     * Handle the return value for the given method endpoint.
     * <p/>
     * This implementation iterates over the set {@linkplain #setMethodReturnValueHandler(java.util.List) return value
     * handlers} to resolve the return value.
     *
     * @param messageContext the current message context
     * @param returnValue    the return value
     * @param methodEndpoint the method endpoint to get arguments for
     * @throws Exception in case of errors
     */
    protected void handleMethodReturnValue(MessageContext messageContext,
                                           Object returnValue,
                                           MethodEndpoint methodEndpoint) throws Exception {
        MethodParameter returnType = methodEndpoint.getReturnType();
        for (MethodReturnValueHandler methodReturnValueHandler : methodReturnValueHandlers) {
            if (methodReturnValueHandler.supportsReturnType(returnType)) {
                methodReturnValueHandler.handleReturnValue(messageContext, returnType, returnValue);
                return;
            }
        }
        throw new IllegalStateException(
                "Return value [" + returnValue + "] not resolved by any MethodReturnValueHandler");
    }
}
