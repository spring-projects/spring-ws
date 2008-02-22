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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.soap.addressing.annotation.Action;
import org.springframework.ws.soap.addressing.annotation.Address;

/**
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class AnnotationActionMethodEndpointMapping extends AbstractActionMethodEndpointMapping
        implements BeanPostProcessor {

    /** Returns the 'endpoint' annotation type. Default is {@link Endpoint}. */
    protected Class<? extends Annotation> getEndpointAnnotationType() {
        return Endpoint.class;
    }

    public final Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public final Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (getEndpointClass(bean).getAnnotation(getEndpointAnnotationType()) != null) {
            registerMethods(bean);
        }
        return bean;
    }

    protected URI getActionForMethod(Method method) {
        Action action = method.getAnnotation(Action.class);
        if (action != null) {
            try {
                return new URI(action.value());
            }
            catch (URISyntaxException e) {
                // ignore
            }
        }
        return null;
    }

    protected Object getEndpointInternal(URI to, URI action) {
        MethodEndpoint methodEndpoint = (MethodEndpoint) lookupEndpoint(action);
        if (methodEndpoint != null) {
            // respect the Address annotation, if set
            Class endpointClass = methodEndpoint.getMethod().getDeclaringClass();
            Address address = AnnotationUtils.findAnnotation(endpointClass, Address.class);
            if (address != null && StringUtils.hasText(address.value())) {
                try {
                    URI addressUri = new URI(address.value());
                    if (to.equals(addressUri)) {
                        return methodEndpoint;
                    }
                }
                catch (URISyntaxException e) {
                    throw new IllegalArgumentException(
                            "Invalid Address annotation [" + address.value() + "] on [" + endpointClass + "]");
                }
            }
            else {
                // address not set
                return methodEndpoint;
            }
        }
        return null;
    }
}
