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
 * @since 1.5.0
 */
class Jaxb1MarshallerBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    public static final String JAXB1_MARSHALLER_CLASS_NAME = "org.springframework.oxm.jaxb.Jaxb1Marshaller";

    protected String getBeanClassName(Element element) {
        return JAXB1_MARSHALLER_CLASS_NAME;
    }
}
