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

package org.springframework.ws.server.endpoint.adapter.method;

import javax.xml.transform.Source;

import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * Abstract base class for {@link MethodArgumentResolver} and {@link MethodReturnValueHandler} implementations based on
 * {@link RequestPayload} and {@link ResponsePayload} annotations.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public abstract class AbstractPayloadMethodProcessor extends TransformerObjectSupport
        implements MethodArgumentResolver, MethodReturnValueHandler {

    // MethodArgumentResolver

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation gets checks if the given parameter is annotated with {@link RequestPayload}, and invokes
     * {@link #supportsRequestPayloadParameter(MethodParameter)} afterwards.
     */
    public final boolean supportsParameter(MethodParameter parameter) {
        Assert.isTrue(parameter.getParameterIndex() >= 0, "Parameter index larger smaller than 0");
        if (parameter.getParameterAnnotation(RequestPayload.class) == null) {
            return false;
        }
        else {
            return supportsRequestPayloadParameter(parameter);
        }
    }

    /**
     * Indicates whether the given {@linkplain MethodParameter method parameter}, annotated with {@link RequestPayload},
     * is supported by this resolver.
     *
     * @param parameter the method parameter to check
     * @return {@code true} if this resolver supports the supplied parameter; {@code false} otherwise
     */
    protected abstract boolean supportsRequestPayloadParameter(MethodParameter parameter);

    public final Object resolveArgument(MessageContext messageContext, MethodParameter parameter) throws Exception {
        Source requestPayload = getRequestPayload(messageContext);
        return requestPayload != null ? resolveRequestPayloadArgument(requestPayload, parameter) : null;
    }

    /** Returns the request payload as {@code Source}. */
    protected Source getRequestPayload(MessageContext messageContext) {
        WebServiceMessage request = messageContext.getRequest();
        return request != null ? request.getPayloadSource() : null;
    }

    /**
     * Resolves the given parameter, annotated with {@link RequestPayload}, into a method argument.
     *
     * @param requestPayload the request payload
     * @param parameter      the parameter to resolve to an argument
     * @return the resolved argument. May be {@code null}.
     * @throws Exception in case of errors
     */
    protected abstract Object resolveRequestPayloadArgument(Source requestPayload, MethodParameter parameter)
            throws Exception;

    // MethodReturnValueHandler

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation gets checks if the method of the given return type is annotated with {@link ResponsePayload},
     * and invokes {@link #supportsResponsePayloadReturnType(MethodParameter)} afterwards.
     */
    public final boolean supportsReturnType(MethodParameter returnType) {
        Assert.isTrue(returnType.getParameterIndex() == -1, "Parameter index is not -1");
        if (returnType.getMethodAnnotation(ResponsePayload.class) == null) {
            return false;
        }
        else {
            return supportsResponsePayloadReturnType(returnType);
        }
    }

    /**
     * Indicates whether the given {@linkplain MethodParameter method return type}, annotated with {@link
     * ResponsePayload}, is supported.
     *
     * @param returnType the method parameter to check
     * @return {@code true} if this resolver supports the supplied return type; {@code false} otherwise
     */
    protected abstract boolean supportsResponsePayloadReturnType(MethodParameter returnType);

    public final void handleReturnValue(MessageContext messageContext, MethodParameter returnType, Object returnValue)
            throws Exception {
        if (returnValue != null) {
            Source responsePayload = createResponsePayload(returnType, returnValue);
            if (responsePayload != null) {
                WebServiceMessage response = messageContext.getResponse();
                transform(responsePayload, response.getPayloadResult());
            }
        }
    }

    /**
     * Creates a response payload for the given return value.
     *
     * @param returnType  the return type to handle
     * @param returnValue the return value to handle
     * @return the response payload
     * @throws Exception in case of errors
     */
    protected abstract Source createResponsePayload(MethodParameter returnType, Object returnValue);
}
