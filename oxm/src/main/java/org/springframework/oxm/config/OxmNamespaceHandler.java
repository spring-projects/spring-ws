/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package org.springframework.oxm.config;

import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * {@link NamespaceHandler} for the '<code>oxm</code>' namespace.
 *
 * @author Arjen Poutsma
 * @since 1.1.0
 */
public class OxmNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("jaxb1-marshaller", new Jaxb1MarshallerBeanDefinitionParser());
        registerBeanDefinitionParser("jaxb2-marshaller", new Jaxb2MarshallerBeanDefinitionParser());
        registerBeanDefinitionParser("jibx-marshaller", new JibxMarshallerBeanDefinitionParser());
        registerBeanDefinitionParser("xmlbeans-marshaller", new XmlBeansMarshallerBeanDefinitionParser());
    }
}
