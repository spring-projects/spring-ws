/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.config;

import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Parser for the <code>&lt;sws:xpath-endpoints/&gt; element.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 * @deprecated as of Spring Web Services 2.0, in favor of {@link AnnotationDrivenBeanDefinitionParser}
 */
@Deprecated
class XPathEndpointsBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

	private static final String XPATH_PARAM_ANNOTATION_METHOD_ENDPOINT_ADAPTER_CLASS_NAME = "org.springframework.ws.server.endpoint.adapter.XPathParamAnnotationMethodEndpointAdapter";

	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}

	@Override
	protected String getBeanClassName(Element element) {
		return XPATH_PARAM_ANNOTATION_METHOD_ENDPOINT_ADAPTER_CLASS_NAME;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder beanDefinitionBuilder) {
		List<Element> namespaceElements = DomUtils.getChildElementsByTagName(element, "namespace");
		if (!namespaceElements.isEmpty()) {
			Properties namespaces = new Properties();
			for (Element namespaceElement : namespaceElements) {
				String prefix = namespaceElement.getAttribute("prefix");
				String uri = namespaceElement.getAttribute("uri");
				namespaces.setProperty(prefix, uri);
			}
			beanDefinitionBuilder.addPropertyValue("namespaces", namespaces);
		}
	}

}
