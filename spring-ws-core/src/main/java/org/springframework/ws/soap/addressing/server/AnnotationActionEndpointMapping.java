/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.soap.addressing.server;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.soap.addressing.core.MessageAddressingProperties;
import org.springframework.ws.soap.addressing.server.annotation.Action;
import org.springframework.ws.soap.addressing.server.annotation.Address;

/**
 * Implementation of the {@link org.springframework.ws.server.EndpointMapping} interface that uses the
 * {@link Action @Action} annotation to map methods to a WS-Addressing {@code Action} header.
 *
 * <p>Endpoints typically have the following form:
 * <pre>
 * &#64;Endpoint
 * &#64;Address("mailto:joe@fabrikam123.example")
 * public class MyEndpoint{
 *	  &#64;Action("https://fabrikam123.example/mail/Delete")
 *	  public Source doSomethingWithRequest() {
 *		 ...
 *	  }
 * }
 * </pre>
 *
 * <p>If set, the {@link Address @Address} annotation on the endpoint class should be equal to the {@link
 * org.springframework.ws.soap.addressing.core.MessageAddressingProperties#getTo() destination} property of the
 * incoming message.
 *
 * @author Arjen Poutsma
 * @see Action
 * @see Address
 * @since 1.5.0
 */
public class AnnotationActionEndpointMapping extends AbstractActionMethodEndpointMapping implements BeanPostProcessor {

	/** Returns the 'endpoint' annotation type. Default is {@link Endpoint}. */
	protected Class<? extends Annotation> getEndpointAnnotationType() {
		return Endpoint.class;
	}

	/**
	 * Returns the action value for the specified method. Default implementation looks for the {@link Action} annotation
	 * value.
	 */
	@Override
	protected URI getActionForMethod(Method method) {
		Action action = method.getAnnotation(Action.class);
		if (action != null && StringUtils.hasText(action.value())) {
			try {
				return new URI(action.value());
			}
			catch (URISyntaxException e) {
				throw new IllegalArgumentException(
						"Invalid Action annotation [" + action.value() + "] on [" + method + "]");
			}
		}
		return null;
	}

	/**
	 * Returns the address property of the given {@link MethodEndpoint}, by looking for the {@link Address} annotation.
	 * The value of this property should match the {@link org.springframework.ws.soap.addressing.core.MessageAddressingProperties#getTo()
	 * destination} of incoming messages. Returns {@code null} if the anotation is not present, thus ignoring the
	 * destination property.
	 *
	 * @param endpoint the method endpoint to return the address for
	 * @return the endpoint address; or {@code null} to ignore the destination property
	 */
	@Override
	protected URI getEndpointAddress(Object endpoint) {
		MethodEndpoint methodEndpoint = (MethodEndpoint) endpoint;
		Class<?> endpointClass = methodEndpoint.getMethod().getDeclaringClass();
		Address address = AnnotationUtils.findAnnotation(endpointClass, Address.class);
		if (address != null && StringUtils.hasText(address.value())) {
			return getActionUri(address.value(), methodEndpoint);
		}
		else {
			return null;
		}
	}

	@Override
	protected URI getResponseAction(Object endpoint, MessageAddressingProperties map) {
		MethodEndpoint methodEndpoint = (MethodEndpoint) endpoint;
		Action action = methodEndpoint.getMethod().getAnnotation(Action.class);
		if (action != null && StringUtils.hasText(action.output())) {
			return getActionUri(action.output(), methodEndpoint);
		}
		else {
			return super.getResponseAction(endpoint, map);
		}
	}

	@Override
	protected URI getFaultAction(Object endpoint, MessageAddressingProperties map) {
		MethodEndpoint methodEndpoint = (MethodEndpoint) endpoint;
		Action action = methodEndpoint.getMethod().getAnnotation(Action.class);
		if (action != null && StringUtils.hasText(action.fault())) {
			return getActionUri(action.fault(), methodEndpoint);
		}
		else {
			return super.getResponseAction(endpoint, map);
		}
	}

	private URI getActionUri(String action, MethodEndpoint methodEndpoint) {
		try {
			return new URI(action);
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException(
					"Invalid Action annotation [" + action + "] on [" + methodEndpoint + "]");
		}
	}

	@Override
	public final Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public final Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (AopUtils.getTargetClass(bean).getAnnotation(getEndpointAnnotationType()) != null) {
			registerMethods(bean);
		}
		return bean;
	}
}
