/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package org.springframework.oxm.config;

import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.oxm.jaxb.Jaxb1Marshaller;

import org.w3c.dom.Element;

/**
 * Parser for the <code>&lt;oxm:jaxb1-marshaller/&gt; element.
 *
 * @author Arjen Poutsma
 * @since 1.1.0
 */
class Jaxb1MarshallerBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    protected Class getBeanClass(Element element) {
        return Jaxb1Marshaller.class;
    }
}
