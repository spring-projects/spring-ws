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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.MethodParameter;
import org.springframework.oxm.GenericMarshaller;
import org.springframework.oxm.GenericUnmarshaller;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.support.MarshallingUtils;

/**
 * Implementation of {@link MethodArgumentResolver} and {@link MethodReturnValueHandler} that uses {@link Marshaller}
 * and {@link Unmarshaller} to support marshalled objects.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class MarshallingPayloadMethodProcessor extends AbstractPayloadMethodProcessor implements InitializingBean {

    private Marshaller marshaller;

    private Unmarshaller unmarshaller;

    public MarshallingPayloadMethodProcessor() {
    }

    public MarshallingPayloadMethodProcessor(Marshaller marshaller) {
        Assert.notNull(marshaller, "marshaller must not be null");
        Assert.isInstanceOf(Unmarshaller.class, marshaller);
        setMarshaller(marshaller);
        setUnmarshaller((Unmarshaller) marshaller);
    }

    public MarshallingPayloadMethodProcessor(Marshaller marshaller, Unmarshaller unmarshaller) {
        Assert.notNull(marshaller, "marshaller must not be null");
        Assert.notNull(unmarshaller, "unmarshaller must not be null");
        setMarshaller(marshaller);
        setUnmarshaller(unmarshaller);
    }

    public Marshaller getMarshaller() {
        return marshaller;
    }

    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public Unmarshaller getUnmarshaller() {
        return unmarshaller;
    }

    public void setUnmarshaller(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(marshaller, "marshaller is required");
        Assert.notNull(unmarshaller, "unmarshaller is required");
    }

    @Override
    protected boolean supportsRequestPayloadParameter(MethodParameter parameter) {
        if (unmarshaller instanceof GenericUnmarshaller) {
            return ((GenericUnmarshaller) unmarshaller).supports(parameter.getGenericParameterType());
        }
        else {
            return unmarshaller.supports(parameter.getParameterType());
        }
    }

    public Object resolveArgument(MessageContext messageContext, MethodParameter parameter) throws Exception {
        WebServiceMessage request = messageContext.getRequest();
        Object argument = MarshallingUtils.unmarshal(getUnmarshaller(), request);
        if (logger.isDebugEnabled()) {
            logger.debug("Unmarshalled payload request to [" + argument + "]");
        }
        return argument;
    }

    @Override
    protected boolean supportsResponsePayloadReturnType(MethodParameter returnType) {
        if (marshaller instanceof GenericMarshaller) {
            GenericMarshaller genericMarshaller = (GenericMarshaller) marshaller;
            return genericMarshaller.supports(returnType.getGenericParameterType());
        }
        else {
            return marshaller.supports(returnType.getParameterType());
        }
    }

    public void handleReturnValue(MessageContext messageContext, MethodParameter returnType, Object returnValue)
            throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Marshalling [" + returnValue + "] to response payload");
        }
        WebServiceMessage response = messageContext.getResponse();
        MarshallingUtils.marshal(getMarshaller(), returnValue, response);
    }

}
