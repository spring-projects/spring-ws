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

package org.springframework.ws.server.endpoint.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.MethodParameter;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.adapter.method.MessageContextMethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;
import org.springframework.ws.server.endpoint.adapter.method.SourcePayloadMethodProcessor;
import org.springframework.ws.server.endpoint.adapter.method.StaxPayloadMethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.XPathParamMethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.dom.Dom4jPayloadMethodProcessor;
import org.springframework.ws.server.endpoint.adapter.method.dom.DomPayloadMethodProcessor;
import org.springframework.ws.server.endpoint.adapter.method.dom.JDomPayloadMethodProcessor;
import org.springframework.ws.server.endpoint.adapter.method.dom.XomPayloadMethodProcessor;
import org.springframework.ws.server.endpoint.adapter.method.jaxb.JaxbElementPayloadMethodProcessor;
import org.springframework.ws.server.endpoint.adapter.method.jaxb.XmlRootElementPayloadMethodProcessor;

/**
 * Default extension of {@link AbstractMethodEndpointAdapter} with support for pluggable
 * {@linkplain MethodArgumentResolver argument resolvers} and {@linkplain MethodReturnValueHandler return value
 * handlers}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class DefaultMethodEndpointAdapter extends AbstractMethodEndpointAdapter
		implements BeanClassLoaderAware, InitializingBean {

	private static final String DOM4J_CLASS_NAME = "org.dom4j.Element";

	private static final String JAXB2_CLASS_NAME = "javax.xml.bind.Binder";

	private static final String JDOM_CLASS_NAME = "org.jdom2.Element";

	private static final String STAX_CLASS_NAME = "javax.xml.stream.XMLInputFactory";

	private static final String XOM_CLASS_NAME = "nu.xom.Element";

	private static final String SOAP_METHOD_ARGUMENT_RESOLVER_CLASS_NAME = "org.springframework.ws.soap.server.endpoint.adapter.method.SoapMethodArgumentResolver";

	private static final String SOAP_HEADER_ELEMENT_ARGUMENT_RESOLVER_CLASS_NAME = "org.springframework.ws.soap.server.endpoint.adapter.method.SoapHeaderElementMethodArgumentResolver";

	private List<MethodArgumentResolver> methodArgumentResolvers;

	private List<MethodArgumentResolver> customMethodArgumentResolvers;

	private List<MethodReturnValueHandler> methodReturnValueHandlers;

	private List<MethodReturnValueHandler> customMethodReturnValueHandlers;

	private ClassLoader classLoader;

	/**
	 * Returns the list of {@code MethodArgumentResolver}s to use.
	 */
	public List<MethodArgumentResolver> getMethodArgumentResolvers() {
		return methodArgumentResolvers;
	}

	/**
	 * Sets the list of {@code MethodArgumentResolver}s to use.
	 */
	public void setMethodArgumentResolvers(List<MethodArgumentResolver> methodArgumentResolvers) {
		this.methodArgumentResolvers = methodArgumentResolvers;
	}

	/**
	 * Returns the custom argument resolvers.
	 */
	public List<MethodArgumentResolver> getCustomMethodArgumentResolvers() {
		return customMethodArgumentResolvers;
	}

	/**
	 * Sets the custom handlers for method arguments. Custom handlers are ordered after built-in ones. To override the
	 * built-in support for return value handling use {@link #setMethodArgumentResolvers(List)}.
	 */
	public void setCustomMethodArgumentResolvers(List<MethodArgumentResolver> customMethodArgumentResolvers) {
		this.customMethodArgumentResolvers = customMethodArgumentResolvers;
	}

	/**
	 * Returns the list of {@code MethodReturnValueHandler}s to use.
	 */
	public List<MethodReturnValueHandler> getMethodReturnValueHandlers() {
		return methodReturnValueHandlers;
	}

	/**
	 * Sets the list of {@code MethodReturnValueHandler}s to use.
	 */
	public void setMethodReturnValueHandlers(List<MethodReturnValueHandler> methodReturnValueHandlers) {
		this.methodReturnValueHandlers = methodReturnValueHandlers;
	}

	/**
	 * Returns the custom return value handlers.
	 */
	public List<MethodReturnValueHandler> getCustomMethodReturnValueHandlers() {
		return customMethodReturnValueHandlers;
	}

	/**
	 * Sets the handlers for custom return value types. Custom handlers are ordered after built-in ones. To override the
	 * built-in support for return value handling use {@link #setMethodReturnValueHandlers(List)}.
	 */
	public void setCustomMethodReturnValueHandlers(List<MethodReturnValueHandler> customMethodReturnValueHandlers) {
		this.customMethodReturnValueHandlers = customMethodReturnValueHandlers;
	}

	private ClassLoader getClassLoader() {
		return this.classLoader != null ? this.classLoader : DefaultMethodEndpointAdapter.class.getClassLoader();
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		initDefaultStrategies();
	}

	/** Initialize the default implementations for the adapter's strategies. */
	protected void initDefaultStrategies() {
		initMethodArgumentResolvers();
		initMethodReturnValueHandlers();
	}

	private void initMethodArgumentResolvers() {
		if (CollectionUtils.isEmpty(methodArgumentResolvers)) {
			List<MethodArgumentResolver> methodArgumentResolvers = new ArrayList<MethodArgumentResolver>();
			methodArgumentResolvers.add(new DomPayloadMethodProcessor());
			methodArgumentResolvers.add(new MessageContextMethodArgumentResolver());
			methodArgumentResolvers.add(new SourcePayloadMethodProcessor());
			methodArgumentResolvers.add(new XPathParamMethodArgumentResolver());
			addMethodArgumentResolver(SOAP_METHOD_ARGUMENT_RESOLVER_CLASS_NAME, methodArgumentResolvers);
			addMethodArgumentResolver(SOAP_HEADER_ELEMENT_ARGUMENT_RESOLVER_CLASS_NAME, methodArgumentResolvers);
			if (isPresent(DOM4J_CLASS_NAME)) {
				methodArgumentResolvers.add(new Dom4jPayloadMethodProcessor());
			}
			if (isPresent(JAXB2_CLASS_NAME)) {
				methodArgumentResolvers.add(new XmlRootElementPayloadMethodProcessor());
				methodArgumentResolvers.add(new JaxbElementPayloadMethodProcessor());
			}
			if (isPresent(JDOM_CLASS_NAME)) {
				methodArgumentResolvers.add(new JDomPayloadMethodProcessor());
			}
			if (isPresent(STAX_CLASS_NAME)) {
				methodArgumentResolvers.add(new StaxPayloadMethodArgumentResolver());
			}
			if (isPresent(XOM_CLASS_NAME)) {
				methodArgumentResolvers.add(new XomPayloadMethodProcessor());
			}
			if (logger.isDebugEnabled()) {
				logger.debug("No MethodArgumentResolvers set, using defaults: " + methodArgumentResolvers);
			}
			if (getCustomMethodArgumentResolvers() != null) {
				methodArgumentResolvers.addAll(getCustomMethodArgumentResolvers());
			}
			setMethodArgumentResolvers(methodArgumentResolvers);
		}
	}

	/**
	 * Certain (SOAP-specific) {@code MethodArgumentResolver}s have to be instantiated by class name, in order to not
	 * introduce a cyclic dependency.
	 */
	@SuppressWarnings("unchecked")
	private void addMethodArgumentResolver(String className, List<MethodArgumentResolver> methodArgumentResolvers) {
		try {
			Class<MethodArgumentResolver> methodArgumentResolverClass = (Class<MethodArgumentResolver>) ClassUtils
					.forName(className, getClassLoader());
			methodArgumentResolvers.add(BeanUtils.instantiateClass(methodArgumentResolverClass));
		} catch (ClassNotFoundException e) {
			logger.warn("Could not find \"" + className + "\" on the classpath");
		}
	}

	private void initMethodReturnValueHandlers() {
		if (CollectionUtils.isEmpty(methodReturnValueHandlers)) {
			List<MethodReturnValueHandler> methodReturnValueHandlers = new ArrayList<MethodReturnValueHandler>();
			methodReturnValueHandlers.add(new DomPayloadMethodProcessor());
			methodReturnValueHandlers.add(new SourcePayloadMethodProcessor());
			if (isPresent(DOM4J_CLASS_NAME)) {
				methodReturnValueHandlers.add(new Dom4jPayloadMethodProcessor());
			}
			if (isPresent(JAXB2_CLASS_NAME)) {
				methodReturnValueHandlers.add(new XmlRootElementPayloadMethodProcessor());
				methodReturnValueHandlers.add(new JaxbElementPayloadMethodProcessor());
			}
			if (isPresent(JDOM_CLASS_NAME)) {
				methodReturnValueHandlers.add(new JDomPayloadMethodProcessor());
			}
			if (isPresent(XOM_CLASS_NAME)) {
				methodReturnValueHandlers.add(new XomPayloadMethodProcessor());
			}
			if (logger.isDebugEnabled()) {
				logger.debug("No MethodReturnValueHandlers set, using defaults: " + methodReturnValueHandlers);
			}
			if (getCustomMethodReturnValueHandlers() != null) {
				methodReturnValueHandlers.addAll(getCustomMethodReturnValueHandlers());
			}
			setMethodReturnValueHandlers(methodReturnValueHandlers);
		}
	}

	private boolean isPresent(String className) {
		return ClassUtils.isPresent(className, getClassLoader());
	}

	@Override
	protected boolean supportsInternal(MethodEndpoint methodEndpoint) {
		return supportsParameters(methodEndpoint.getMethodParameters())
				&& supportsReturnType(methodEndpoint.getReturnType());
	}

	private boolean supportsParameters(MethodParameter[] methodParameters) {
		for (MethodParameter methodParameter : methodParameters) {
			boolean supported = false;
			for (MethodArgumentResolver methodArgumentResolver : methodArgumentResolvers) {
				if (logger.isTraceEnabled()) {
					logger.trace("Testing if argument resolver [" + methodArgumentResolver + "] supports ["
							+ methodParameter.getGenericParameterType() + "]");
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
			logger.trace("Invoking [" + methodEndpoint + "] with arguments " + Arrays.asList(args));
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
	 * <p>
	 * This implementation iterates over the set {@linkplain #setMethodArgumentResolvers(List) argument resolvers} to
	 * resolve each argument.
	 *
	 * @param messageContext the current message context
	 * @param methodEndpoint the method endpoint to get arguments for
	 * @return the arguments
	 * @throws Exception in case of errors
	 */
	protected Object[] getMethodArguments(MessageContext messageContext, MethodEndpoint methodEndpoint) throws Exception {
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
	 * <p>
	 * This implementation iterates over the set {@linkplain #setMethodReturnValueHandlers(java.util.List)} return value
	 * handlers} to resolve the return value.
	 *
	 * @param messageContext the current message context
	 * @param returnValue the return value
	 * @param methodEndpoint the method endpoint to get arguments for
	 * @throws Exception in case of errors
	 */
	protected void handleMethodReturnValue(MessageContext messageContext, Object returnValue,
			MethodEndpoint methodEndpoint) throws Exception {
		MethodParameter returnType = methodEndpoint.getReturnType();
		for (MethodReturnValueHandler methodReturnValueHandler : methodReturnValueHandlers) {
			if (methodReturnValueHandler.supportsReturnType(returnType)) {
				methodReturnValueHandler.handleReturnValue(messageContext, returnType, returnValue);
				return;
			}
		}
		throw new IllegalStateException("Return value [" + returnValue + "] not resolved by any MethodReturnValueHandler");
	}
}
