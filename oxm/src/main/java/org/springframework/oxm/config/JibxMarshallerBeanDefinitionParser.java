/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package org.springframework.oxm.config;

import org.w3c.dom.Element;

import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;

/**
 * Parser for the <code>&lt;oxm:jibx-marshaller/&gt; element.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
class JibxMarshallerBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    private static final String JIBX_MARSHALLER_CLASS_NAME = "org.springframework.oxm.jibx.JibxMarshaller";

    protected String getBeanClassName(Element element) {
        return JIBX_MARSHALLER_CLASS_NAME;
    }

}