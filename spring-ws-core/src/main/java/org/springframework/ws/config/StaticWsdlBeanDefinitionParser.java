/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.config;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser for the {@code &lt;sws:static-wsdl/&gt;} element.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
class StaticWsdlBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	private static final String CLASS_NAME = "org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition";

	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}

	@Override
	protected String getBeanClassName(Element element) {
		return CLASS_NAME;
	}

	@Override
	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext)
			throws BeanDefinitionStoreException {
		String id = element.getAttribute(ID_ATTRIBUTE);
		if (StringUtils.hasLength(id)) {
			return id;
		}
		String location = element.getAttribute("location");
		if (StringUtils.hasLength(location)) {
			String filename = StringUtils.stripFilenameExtension(StringUtils.getFilename(location));
			if (StringUtils.hasLength(filename)) {
				return filename;
			}
		}
		return parserContext.getReaderContext().generateBeanName(definition);
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder beanDefinitionBuilder) {
		String location = element.getAttribute("location");
		beanDefinitionBuilder.addPropertyValue("wsdl", location);
	}

}
