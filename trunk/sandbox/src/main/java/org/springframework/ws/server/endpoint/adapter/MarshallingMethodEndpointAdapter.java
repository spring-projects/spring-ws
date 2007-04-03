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

package org.springframework.ws.server.endpoint.adapter;

import java.lang.reflect.Method;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.server.endpoint.MethodEndpoint;

/**
 * Adapter that supports endpoint methods that use marshalling. Supports methods with the following signature:
 * <pre>
 * void handleMyMessage(MyUnmarshalledType request);
 * </pre>
 * or
 * <pre>
 * MyMarshalledType handleMyMessage(MyUnmarshalledType request);
 * </pre>
 * I.e. methods that take a single parameter that {@link Unmarshaller#supports(Class) is supported} by the {@link
 * Unmarshaller}, and return either <code>void</code> or a type {@link Marshaller#supports(Class) supported} by the
 * {@link Marshaller}. The method can have any name, as long as it is mapped by an {@link EndpointMapping}.
 * <p/>
 * This endpoint needs a <code>Marshaller</code> and <code>Unmarshaller</code>, both of which can be set using
 * properties.
 *
 * @author Arjen Poutsma
 * @see #setMarshaller(org.springframework.oxm.Marshaller)
 * @see #setUnmarshaller(org.springframework.oxm.Unmarshaller)
 */
public class MarshallingMethodEndpointAdapter extends AbstractMethodEndpointAdapter implements InitializingBean {

    private Marshaller marshaller;

    private Unmarshaller unmarshaller;

    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public void setUnmarshaller(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(marshaller, "marshaller is required");
        Assert.notNull(unmarshaller, "unmarshaller is required");
    }

    /**
     * Supports a method with a single, unmarshallable parameter, and that return <code>void</code> or a marshallable
     * type.
     *
     * @see Marshaller#supports(Class)
     * @see Unmarshaller#supports(Class)
     */
    protected boolean supportsInternal(MethodEndpoint methodEndpoint) {
        Method method = methodEndpoint.getMethod();
        return (Void.TYPE.isAssignableFrom(method.getReturnType()) || marshaller.supports(method.getReturnType())) &&
                method.getParameterTypes().length == 1 && unmarshaller.supports(method.getParameterTypes()[0]);
    }

    protected void invokeInternal(MessageContext messageContext, MethodEndpoint methodEndpoint) throws Exception {
        WebServiceMessage request = messageContext.getRequest();
        Object requestObject = unmarshaller.unmarshal(request.getPayloadSource());
        if (logger.isDebugEnabled()) {
            logger.debug("Unmarshalled payload request to [" + requestObject + "]");
        }
        Object responseObject = methodEndpoint.invoke(new Object[]{requestObject});
        if (responseObject != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Marshalling [" + responseObject + "] to response payload");
            }
            WebServiceMessage response = messageContext.getResponse();
            marshaller.marshal(responseObject, response.getPayloadResult());
        }
    }
}
