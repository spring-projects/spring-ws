/*
 * Copyright 2005-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.config;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.adapter.method.MarshallingPayloadMethodProcessor;
import org.springframework.ws.server.endpoint.adapter.method.MessageContextMethodArgumentResolver;
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
 * {@link BeanDefinitionParser} that parses the {@code annotation-driven} element to
 * configure a Spring WS application.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
class AnnotationDrivenBeanDefinitionParser implements BeanDefinitionParser {

	private static final boolean dom4jPresent = ClassUtils.isPresent("org.dom4j.Element",
			AnnotationDrivenBeanDefinitionParser.class.getClassLoader());

	private static final boolean jaxb2Present = ClassUtils.isPresent("jakarta.xml.bind.Binder",
			AnnotationDrivenBeanDefinitionParser.class.getClassLoader());

	private static final boolean jdomPresent = ClassUtils.isPresent("org.jdom2.Element",
			AnnotationDrivenBeanDefinitionParser.class.getClassLoader());

	private static final boolean staxPresent = ClassUtils.isPresent("javax.xml.stream.XMLInputFactory",
			AnnotationDrivenBeanDefinitionParser.class.getClassLoader());

	private static final boolean xomPresent = ClassUtils.isPresent("nu.xom.Element",
			AnnotationDrivenBeanDefinitionParser.class.getClassLoader());

	@Override
	public @Nullable BeanDefinition parse(Element element, ParserContext parserContext) {
		Object source = parserContext.extractSource(element);

		CompositeComponentDefinition compDefinition = new CompositeComponentDefinition(element.getTagName(), source);
		parserContext.pushContainingComponent(compDefinition);

		registerEndpointMappings(parserContext, source);

		registerEndpointAdapters(parserContext, element, source);

		registerEndpointExceptionResolvers(parserContext, source);

		parserContext.popAndRegisterContainingComponent();

		return null;
	}

	private void registerEndpointMappings(ParserContext parserContext, @Nullable Object source) {
		RootBeanDefinition payloadRootMappingDef = createBeanDefinition(
				PayloadRootAnnotationMethodEndpointMapping.class, source);
		payloadRootMappingDef.getPropertyValues().add("order", 0);
		parserContext.getReaderContext().registerWithGeneratedName(payloadRootMappingDef);

		RootBeanDefinition soapActionMappingDef = createBeanDefinition(SoapActionAnnotationMethodEndpointMapping.class,
				source);
		soapActionMappingDef.getPropertyValues().add("order", 1);
		parserContext.getReaderContext().registerWithGeneratedName(soapActionMappingDef);

		RootBeanDefinition annActionMappingDef = createBeanDefinition(AnnotationActionEndpointMapping.class, source);
		annActionMappingDef.getPropertyValues().add("order", 2);
		parserContext.getReaderContext().registerWithGeneratedName(annActionMappingDef);
	}

	private void registerEndpointAdapters(ParserContext parserContext, Element element, @Nullable Object source) {
		RootBeanDefinition adapterDef = createBeanDefinition(DefaultMethodEndpointAdapter.class, source);

		ManagedList<BeanMetadataElement> argumentResolvers = new ManagedList<>();
		argumentResolvers.setSource(source);

		ManagedList<BeanMetadataElement> returnValueHandlers = new ManagedList<>();
		returnValueHandlers.setSource(source);

		argumentResolvers.add(createBeanDefinition(MessageContextMethodArgumentResolver.class, source));
		argumentResolvers.add(createBeanDefinition(XPathParamMethodArgumentResolver.class, source));
		argumentResolvers.add(createBeanDefinition(SoapMethodArgumentResolver.class, source));
		argumentResolvers.add(createBeanDefinition(SoapHeaderElementMethodArgumentResolver.class, source));

		RuntimeBeanReference domProcessor = createBeanReference(parserContext, DomPayloadMethodProcessor.class, source);
		argumentResolvers.add(domProcessor);
		returnValueHandlers.add(domProcessor);

		RuntimeBeanReference sourceProcessor = createBeanReference(parserContext, SourcePayloadMethodProcessor.class,
				source);
		argumentResolvers.add(sourceProcessor);
		returnValueHandlers.add(sourceProcessor);

		if (dom4jPresent) {
			RuntimeBeanReference dom4jProcessor = createBeanReference(parserContext, Dom4jPayloadMethodProcessor.class,
					source);
			argumentResolvers.add(dom4jProcessor);
			returnValueHandlers.add(dom4jProcessor);
		}
		if (jaxb2Present) {
			RuntimeBeanReference xmlRootElementProcessor = createBeanReference(parserContext,
					XmlRootElementPayloadMethodProcessor.class, source);
			argumentResolvers.add(xmlRootElementProcessor);
			returnValueHandlers.add(xmlRootElementProcessor);

			RuntimeBeanReference jaxbElementProcessor = createBeanReference(parserContext,
					JaxbElementPayloadMethodProcessor.class, source);
			argumentResolvers.add(jaxbElementProcessor);
			returnValueHandlers.add(jaxbElementProcessor);
		}
		if (jdomPresent) {
			RuntimeBeanReference jdomProcessor = createBeanReference(parserContext, JDomPayloadMethodProcessor.class,
					source);
			argumentResolvers.add(jdomProcessor);
			returnValueHandlers.add(jdomProcessor);
		}
		if (staxPresent) {
			argumentResolvers.add(createBeanDefinition(StaxPayloadMethodArgumentResolver.class, source));
		}
		if (xomPresent) {
			RuntimeBeanReference xomProcessor = createBeanReference(parserContext, XomPayloadMethodProcessor.class,
					source);
			argumentResolvers.add(xomProcessor);
			returnValueHandlers.add(xomProcessor);
		}
		if (element.hasAttribute("marshaller")) {
			RuntimeBeanReference marshallerReference = new RuntimeBeanReference(element.getAttribute("marshaller"));
			RuntimeBeanReference unmarshallerReference;
			if (element.hasAttribute("unmarshaller")) {
				unmarshallerReference = new RuntimeBeanReference(element.getAttribute("unmarshaller"));
			}
			else {
				unmarshallerReference = marshallerReference;
			}

			RootBeanDefinition marshallingProcessorDef = createBeanDefinition(MarshallingPayloadMethodProcessor.class,
					source);
			marshallingProcessorDef.getPropertyValues().add("marshaller", marshallerReference);
			marshallingProcessorDef.getPropertyValues().add("unmarshaller", unmarshallerReference);
			argumentResolvers.add(marshallingProcessorDef);
			returnValueHandlers.add(marshallingProcessorDef);
		}

		adapterDef.getPropertyValues().add("methodArgumentResolvers", argumentResolvers);
		adapterDef.getPropertyValues().add("methodReturnValueHandlers", returnValueHandlers);

		parserContext.getReaderContext().registerWithGeneratedName(adapterDef);
	}

	private void registerEndpointExceptionResolvers(ParserContext parserContext, @Nullable Object source) {
		RootBeanDefinition annotationResolverDef = createBeanDefinition(SoapFaultAnnotationExceptionResolver.class,
				source);
		annotationResolverDef.getPropertyValues().add("order", 0);
		parserContext.getReaderContext().registerWithGeneratedName(annotationResolverDef);

		RootBeanDefinition simpleResolverDef = createBeanDefinition(SimpleSoapExceptionResolver.class, source);
		simpleResolverDef.getPropertyValues().add("order", Ordered.LOWEST_PRECEDENCE);
		parserContext.getReaderContext().registerWithGeneratedName(simpleResolverDef);
	}

	private RuntimeBeanReference createBeanReference(ParserContext parserContext, Class<?> beanClass,
			@Nullable Object source) {
		RootBeanDefinition beanDefinition = createBeanDefinition(beanClass, source);
		String beanName = parserContext.getReaderContext().registerWithGeneratedName(beanDefinition);
		parserContext.registerComponent(new BeanComponentDefinition(beanDefinition, beanName));
		return new RuntimeBeanReference(beanName);
	}

	private RootBeanDefinition createBeanDefinition(Class<?> beanClass, @Nullable Object source) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(beanClass);
		beanDefinition.setSource(source);
		beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		return beanDefinition;
	}

}
