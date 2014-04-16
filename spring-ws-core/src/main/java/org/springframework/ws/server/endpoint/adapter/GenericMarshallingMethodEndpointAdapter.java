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

import java.lang.reflect.Method;

import org.springframework.oxm.GenericMarshaller;
import org.springframework.oxm.GenericUnmarshaller;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.server.endpoint.MethodEndpoint;

/**
 * Subclass of {@link MarshallingMethodEndpointAdapter} that supports {@link GenericMarshaller} and {@link
 * GenericUnmarshaller}. More specifically, this adapter is aware of the {@link Method#getGenericParameterTypes()} and
 * {@link Method#getGenericReturnType()}.
 *
 * <p>Prefer to use this adapter rather than the plain {@link MarshallingMethodEndpointAdapter} in combination with Java 5
 * marshallers, such as the {@link Jaxb2Marshaller}.
 *
 * @author Arjen Poutsma
 * @since 1.0.2
 * @deprecated as of Spring Web Services 2.0, in favor of {@link DefaultMethodEndpointAdapter} and {@link
 *             org.springframework.ws.server.endpoint.adapter.method.MarshallingPayloadMethodProcessor
 *             MarshallingPayloadMethodProcessor}.
 */
@Deprecated
public class GenericMarshallingMethodEndpointAdapter extends MarshallingMethodEndpointAdapter {

    /**
     * Creates a new {@code GenericMarshallingMethodEndpointAdapter}. The {@link Marshaller} and {@link
     * Unmarshaller} must be injected using properties.
     *
     * @see #setMarshaller(org.springframework.oxm.Marshaller)
     * @see #setUnmarshaller(org.springframework.oxm.Unmarshaller)
     */
    public GenericMarshallingMethodEndpointAdapter() {
    }

    /**
     * Creates a new {@code GenericMarshallingMethodEndpointAdapter} with the given marshaller. If the given {@link
     * Marshaller} also implements the {@link Unmarshaller} interface, it is used for both marshalling and
     * unmarshalling. Otherwise, an exception is thrown.
     *
     * <p>Note that all {@link Marshaller} implementations in Spring-WS also implement the {@link Unmarshaller} interface,
     * so that you can safely use this constructor.
     *
     * @param marshaller object used as marshaller and unmarshaller
     * @throws IllegalArgumentException when {@code marshaller} does not implement the {@link Unmarshaller}
     *                                  interface
     */
    public GenericMarshallingMethodEndpointAdapter(Marshaller marshaller) {
        super(marshaller);
    }

    /**
     * Creates a new {@code GenericMarshallingMethodEndpointAdapter} with the given marshaller and unmarshaller.
     *
     * @param marshaller   the marshaller to use
     * @param unmarshaller the unmarshaller to use
     */
    public GenericMarshallingMethodEndpointAdapter(Marshaller marshaller, Unmarshaller unmarshaller) {
        super(marshaller, unmarshaller);
    }

    @Override
    protected boolean supportsInternal(MethodEndpoint methodEndpoint) {
        Method method = methodEndpoint.getMethod();
        return supportsReturnType(method) && supportsParameters(method);
    }

    private boolean supportsReturnType(Method method) {
        if (Void.TYPE.equals(method.getReturnType())) {
            return true;
        }
        else {
            if (getMarshaller() instanceof GenericMarshaller) {
                return ((GenericMarshaller) getMarshaller()).supports(method.getGenericReturnType());
            }
            else {
                return getMarshaller().supports(method.getReturnType());
            }
        }
    }

    private boolean supportsParameters(Method method) {
        if (method.getParameterTypes().length != 1) {
            return false;
        }
        else if (getUnmarshaller() instanceof GenericUnmarshaller) {
            GenericUnmarshaller genericUnmarshaller = (GenericUnmarshaller) getUnmarshaller();
            return genericUnmarshaller.supports(method.getGenericParameterTypes()[0]);
        }
        else {
            return getUnmarshaller().supports(method.getParameterTypes()[0]);
        }
    }
}
