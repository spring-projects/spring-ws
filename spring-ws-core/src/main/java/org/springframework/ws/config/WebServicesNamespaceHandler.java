/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.config;

import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * {@link NamespaceHandler} for the '{@code web-services}' namespace.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class WebServicesNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("annotation-driven", new AnnotationDrivenBeanDefinitionParser());
		registerBeanDefinitionParser("interceptors", new InterceptorsBeanDefinitionParser());
		registerBeanDefinitionParser("static-wsdl", new StaticWsdlBeanDefinitionParser());
		registerBeanDefinitionParser("dynamic-wsdl", new DynamicWsdlBeanDefinitionParser());
	}

}
