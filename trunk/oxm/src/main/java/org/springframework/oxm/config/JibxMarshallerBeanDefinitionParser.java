/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package org.springframework.oxm.config;

import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.oxm.jibx.JibxMarshaller;
import org.w3c.dom.Element;

/**
 * Parser for the <code>&lt;oxm:jibx-marshaller/&gt; element.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
class JibxMarshallerBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    private static final String JIBX_MARSHALLER_CLASS_NAME = "org.springframework.oxm.jibx.JibxMarshaller";

    protected String getParentName(Element element) {
        return JIBX_MARSHALLER_CLASS_NAME;
    }

}