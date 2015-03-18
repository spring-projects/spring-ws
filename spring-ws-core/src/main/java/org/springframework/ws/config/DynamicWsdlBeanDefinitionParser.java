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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.commons.CommonsXsdSchemaCollection;

import org.w3c.dom.Element;

/**
 * Parser for the {@code &lt;sws:dynamic-wsdl/&gt;} element.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
class DynamicWsdlBeanDefinitionParser extends AbstractBeanDefinitionParser {

	private static final boolean commonsSchemaPresent = ClassUtils.isPresent("org.apache.ws.commons.schema.XmlSchema",
			DynamicWsdlBeanDefinitionParser.class.getClassLoader());

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		Object source = parserContext.extractSource(element);

		BeanDefinitionBuilder wsdlBuilder = BeanDefinitionBuilder.rootBeanDefinition(DefaultWsdl11Definition.class);
		wsdlBuilder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		wsdlBuilder.getRawBeanDefinition().setSource(source);

		addProperty(element, wsdlBuilder, "portTypeName");
		addProperty(element, wsdlBuilder, "targetNamespace");
		addProperty(element, wsdlBuilder, "requestSuffix");
		addProperty(element, wsdlBuilder, "responseSuffix");
		addProperty(element, wsdlBuilder, "faultSuffix");
		addProperty(element, wsdlBuilder, "createSoap11Binding");
		addProperty(element, wsdlBuilder, "createSoap12Binding");
		addProperty(element, wsdlBuilder, "transportUri");
		addProperty(element, wsdlBuilder, "locationUri");
		addProperty(element, wsdlBuilder, "serviceName");

		List<Element> schemas = DomUtils.getChildElementsByTagName(element, "xsd");
		if (commonsSchemaPresent) {
			RootBeanDefinition collectionDef = createBeanDefinition(CommonsXsdSchemaCollection.class, source);
			collectionDef.getPropertyValues().addPropertyValue("inline", "true");
			ManagedList<String> xsds = new ManagedList<String>();
			xsds.setSource(source);
			for (Element schema : schemas) {
				xsds.add(schema.getAttribute("location"));
			}
			collectionDef.getPropertyValues().addPropertyValue("xsds", xsds);
			String collectionName = parserContext.getReaderContext().registerWithGeneratedName(collectionDef);
			wsdlBuilder.addPropertyReference("schemaCollection", collectionName);
		}
		else {
			if (schemas.size() > 1) {
				throw new IllegalArgumentException(
						"Multiple <xsd/> elements requires Commons XMLSchema." +
								"Please put Commons XMLSchema on the classpath.");
			}
			RootBeanDefinition schemaDef = createBeanDefinition(SimpleXsdSchema.class, source);
			Element schema = schemas.iterator().next();
			schemaDef.getPropertyValues().addPropertyValue("xsd", schema.getAttribute("location"));
			String schemaName = parserContext.getReaderContext().registerWithGeneratedName(schemaDef);
			wsdlBuilder.addPropertyReference("schema", schemaName);
		}
		return wsdlBuilder.getBeanDefinition();
	}

	private void addProperty(Element element, BeanDefinitionBuilder builder, String propertyName) {
		String propertyValue = element.getAttribute(propertyName);
		if (StringUtils.hasText(propertyValue)) {
			builder.addPropertyValue(propertyName, propertyValue);
		}
	}

	private RootBeanDefinition createBeanDefinition(Class<?> beanClass, Object source) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(beanClass);
		beanDefinition.setSource(source);
		beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		return beanDefinition;
	}

}
