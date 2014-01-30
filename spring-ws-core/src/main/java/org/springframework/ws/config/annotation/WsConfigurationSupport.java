/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.config.annotation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.ws.server.EndpointAdapter;
import org.springframework.ws.server.EndpointExceptionResolver;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.mapping.PayloadRootAnnotationMethodEndpointMapping;
import org.springframework.ws.soap.addressing.server.AnnotationActionEndpointMapping;
import org.springframework.ws.soap.addressing.server.annotation.Action;
import org.springframework.ws.soap.server.endpoint.SimpleSoapExceptionResolver;
import org.springframework.ws.soap.server.endpoint.SoapFaultAnnotationExceptionResolver;
import org.springframework.ws.soap.server.endpoint.annotation.SoapAction;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;
import org.springframework.ws.soap.server.endpoint.mapping.SoapActionAnnotationMethodEndpointMapping;

/**
 * This is the main class providing the configuration behind the Spring Web Services Java
 * config. It is typically imported by adding {@link EnableWs @EnableWs} to an
 * application {@link Configuration @Configuration} class. An alternative, more
 * advanced option is to extend directly from this class and override methods as
 * necessary remembering to add {@link Configuration @Configuration} to the
 * subclass and {@link Bean @Bean} to overridden {@link Bean @Bean} methods.
 * For more details see the Javadoc of {@link EnableWs @EnableWs}.
 *
 * <p>This class registers the following {@link EndpointMapping}s:
 * <ul>
 * 	<li>{@link PayloadRootAnnotationMethodEndpointMapping}
 * 	ordered at 0 for mapping requests to {@link PayloadRoot @PayloadRoot} annotated
 * 	controller methods.
 * 	<li>{@link SoapActionAnnotationMethodEndpointMapping}
 * 	ordered at 1 for mapping requests to {@link SoapAction @SoapAction} annotated
 * 	controller methods.
 * 	<li>{@link AnnotationActionEndpointMapping}
 * 	ordered at 2 for mapping requests to {@link Action @Action} annotated
 * 	controller methods.
 * </ul>
 *
 * <p>Registers one {@link EndpointAdapter}:
 * <ul>
 * 	<li>{@link DefaultMethodEndpointAdapter}
 * 	for processing requests with annotated endpoint methods.
 * </ul>
 *
 * <p>Registers the following {@link EndpointExceptionResolver}s:
 * <ul>
 * 	<li>{@link SoapFaultAnnotationExceptionResolver} for handling exceptions
 * 	annotated with {@link SoapFault @SoapFault}.
 * 	<li>{@link SimpleSoapExceptionResolver} for creating default exceptions.
 * </ul>
 *
 * @see EnableWs
 * @see WsConfigurer
 * @see WsConfigurerAdapter
 *
 * @author Arjen Poutsma
 * @since 2.2
 */
public class WsConfigurationSupport {

	private List<EndpointInterceptor> interceptors;

	/**
	 * Returns a {@link PayloadRootAnnotationMethodEndpointMapping} ordered at 0 for
	 * mapping requests to annotated endpoints.
	 */
	@Bean
	public PayloadRootAnnotationMethodEndpointMapping payloadRootAnnotationMethodEndpointMapping() {
		PayloadRootAnnotationMethodEndpointMapping endpointMapping =
				new PayloadRootAnnotationMethodEndpointMapping();
		endpointMapping.setOrder(0);
		endpointMapping.setInterceptors(getInterceptors());
		return endpointMapping;
	}

	/**
	 * Returns a {@link SoapActionAnnotationMethodEndpointMapping} ordered at 1 for
	 * mapping requests to annotated endpoints.
	 */
	@Bean
	public SoapActionAnnotationMethodEndpointMapping soapActionAnnotationMethodEndpointMapping() {
		SoapActionAnnotationMethodEndpointMapping endpointMapping =
				new SoapActionAnnotationMethodEndpointMapping();
		endpointMapping.setOrder(1);
		endpointMapping.setInterceptors(getInterceptors());
		return endpointMapping;
	}

	/**
	 * Returns a {@link AnnotationActionEndpointMapping} ordered at 2 for
	 * mapping requests to annotated endpoints.
	 */
	@Bean
	public AnnotationActionEndpointMapping annotationActionEndpointMapping() {
		AnnotationActionEndpointMapping endpointMapping =
				new AnnotationActionEndpointMapping();
		endpointMapping.setOrder(2);
		endpointMapping.setPostInterceptors(getInterceptors());
		return endpointMapping;
	}

	/**
	 * Provide access to the shared handler interceptors used to configure
	 * {@link EndpointMapping} instances with. This method cannot be overridden,
	 * use {@link #addInterceptors(List)} instead.
	 */
	protected final EndpointInterceptor[] getInterceptors() {
		if (interceptors == null) {
			interceptors = new ArrayList<EndpointInterceptor>();
			addInterceptors(interceptors);
		}
		return interceptors.toArray(new EndpointInterceptor[interceptors.size()]);
	}

	/**
	 * Template method to add endpoint interceptors. Override this method to add Spring-WS
	 * interceptors for pre- and post-processing of endpoint invocation.
	 */
	protected void addInterceptors(List<EndpointInterceptor> interceptors) {
	}

	/**
	 * Returns a {@link DefaultMethodEndpointAdapter} for processing requests
	 * through annotated endpoint methods. Consider overriding one of these
	 * other more fine-grained methods:
	 * <ul>
	 *  <li>{@link #addArgumentResolvers(List)} for adding custom argument resolvers.
	 * 	<li>{@link #addReturnValueHandlers(List)} for adding custom return value handlers.
	 * </ul>
	 */
	@Bean
	public DefaultMethodEndpointAdapter defaultMethodEndpointAdapter() {
		List<MethodArgumentResolver> argumentResolvers =
				new ArrayList<MethodArgumentResolver>();
		addArgumentResolvers(argumentResolvers);

		List<MethodReturnValueHandler> returnValueHandlers =
				new ArrayList<MethodReturnValueHandler>();
		addReturnValueHandlers(returnValueHandlers);

		DefaultMethodEndpointAdapter adapter = new DefaultMethodEndpointAdapter();
		adapter.setCustomMethodArgumentResolvers(argumentResolvers);
		adapter.setCustomMethodReturnValueHandlers(returnValueHandlers);

		return adapter;
	}

	/**
	 * Add custom {@link MethodArgumentResolver}s to use in addition to
	 * the ones registered by default.
	 * @param argumentResolvers the list of custom converters;
	 * 	initially an empty list.
	 */
	protected void addArgumentResolvers(List<MethodArgumentResolver> argumentResolvers) {
	}

	/**
	 * Add custom {@link MethodReturnValueHandler}s in addition to the
	 * ones registered by default.
	 * @param returnValueHandlers the list of custom handlers;
	 * initially an empty list.
	 */
	protected void addReturnValueHandlers(
			List<MethodReturnValueHandler> returnValueHandlers) {
	}

	/**
	 * Returns a {@link SoapFaultAnnotationExceptionResolver} ordered at 0 for handling
	 * endpoint exceptions.
	 */
	@Bean
	public SoapFaultAnnotationExceptionResolver soapFaultAnnotationExceptionResolver() {
		SoapFaultAnnotationExceptionResolver exceptionResolver = new SoapFaultAnnotationExceptionResolver();
		exceptionResolver.setOrder(0);

		return exceptionResolver;
	}

	/**
	 * Returns a {@link SimpleSoapExceptionResolver} ordered at
	 * {@linkplain Ordered#LOWEST_PRECEDENCE lowest precedence} for handling endpoint
	 * exceptions.
	 */
	@Bean
	public SimpleSoapExceptionResolver simpleSoapExceptionResolver() {
		SimpleSoapExceptionResolver exceptionResolver = new SimpleSoapExceptionResolver();
		exceptionResolver.setOrder(Ordered.LOWEST_PRECEDENCE);

		return exceptionResolver;
	}

}
