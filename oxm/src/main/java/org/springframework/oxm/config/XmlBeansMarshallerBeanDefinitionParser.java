/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package org.springframework.oxm.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.oxm.xmlbeans.XmlBeansMarshaller;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser for the <code>&lt;oxm:xmlbeans-marshaller/&gt; element.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
class XmlBeansMarshallerBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    protected Class getBeanClass(Element element) {
        return XmlBeansMarshaller.class;
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder beanDefinitionBuilder) {
        String optionsName = element.getAttribute("options");
        if (StringUtils.hasText(optionsName)) {
            beanDefinitionBuilder.addPropertyReference("xmlOptions", optionsName);
        }
    }
}