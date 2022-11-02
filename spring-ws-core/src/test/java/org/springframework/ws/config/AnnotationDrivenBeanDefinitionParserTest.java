/*
 * Copyright 2005-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.config;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ws.server.EndpointAdapter;
import org.springframework.ws.server.EndpointExceptionResolver;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
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
import org.springframework.ws.server.endpoint.mapping.PayloadRootAnnotationMethodEndpointMapping;
import org.springframework.ws.soap.addressing.server.AnnotationActionEndpointMapping;
import org.springframework.ws.soap.server.endpoint.SimpleSoapExceptionResolver;
import org.springframework.ws.soap.server.endpoint.SoapFaultAnnotationExceptionResolver;
import org.springframework.ws.soap.server.endpoint.adapter.method.SoapHeaderElementMethodArgumentResolver;
import org.springframework.ws.soap.server.endpoint.adapter.method.SoapMethodArgumentResolver;
import org.springframework.ws.soap.server.endpoint.mapping.SoapActionAnnotationMethodEndpointMapping;

/**
 * @author Arjen Poutsma
 */
public class AnnotationDrivenBeanDefinitionParserTest {

	private ApplicationContext applicationContext;

	@BeforeEach
	public void setUp() throws Exception {
		applicationContext = new ClassPathXmlApplicationContext("annotationDrivenBeanDefinitionParserTest.xml", getClass());
	}

	@Test
	public void endpointMappings() {

		Map<String, EndpointMapping> result = applicationContext.getBeansOfType(EndpointMapping.class);

		assertThat(result).hasSize(3);
		assertThat(result.values()).hasAtLeastOneElementOfType(PayloadRootAnnotationMethodEndpointMapping.class);
		assertThat(result.values()).hasAtLeastOneElementOfType(SoapActionAnnotationMethodEndpointMapping.class);
		assertThat(result.values()).hasAtLeastOneElementOfType(AnnotationActionEndpointMapping.class);
	}

	@Test
	public void endpointAdapters() {

		Map<String, EndpointAdapter> result = applicationContext.getBeansOfType(EndpointAdapter.class);

		assertThat(result).hasSize(1);

		DefaultMethodEndpointAdapter endpointAdapter = (DefaultMethodEndpointAdapter) result.values().iterator().next();

		List<MethodArgumentResolver> argumentResolvers = endpointAdapter.getMethodArgumentResolvers();

		assertThat(argumentResolvers).isNotEmpty();
		assertThat(argumentResolvers).hasOnlyElementsOfTypes(MessageContextMethodArgumentResolver.class,
				XPathParamMethodArgumentResolver.class, SoapMethodArgumentResolver.class,
				SoapHeaderElementMethodArgumentResolver.class, DomPayloadMethodProcessor.class,
				SourcePayloadMethodProcessor.class, Dom4jPayloadMethodProcessor.class,
				XmlRootElementPayloadMethodProcessor.class, JaxbElementPayloadMethodProcessor.class,
				JDomPayloadMethodProcessor.class, StaxPayloadMethodArgumentResolver.class, XomPayloadMethodProcessor.class);

		List<MethodReturnValueHandler> returnValueHandlers = endpointAdapter.getMethodReturnValueHandlers();

		assertThat(returnValueHandlers).isNotEmpty();
		assertThat(returnValueHandlers).hasOnlyElementsOfTypes(DomPayloadMethodProcessor.class,
				SourcePayloadMethodProcessor.class, Dom4jPayloadMethodProcessor.class,
				XmlRootElementPayloadMethodProcessor.class, JaxbElementPayloadMethodProcessor.class,
				JDomPayloadMethodProcessor.class, XomPayloadMethodProcessor.class);
	}

	@Test
	public void endpointExceptionResolver() {

		Map<String, EndpointExceptionResolver> result = applicationContext.getBeansOfType(EndpointExceptionResolver.class);

		assertThat(result).hasSize(2);
		assertThat(result.values()).hasAtLeastOneElementOfType(SoapFaultAnnotationExceptionResolver.class);
		assertThat(result.values()).hasAtLeastOneElementOfType(SimpleSoapExceptionResolver.class);
	}
}
