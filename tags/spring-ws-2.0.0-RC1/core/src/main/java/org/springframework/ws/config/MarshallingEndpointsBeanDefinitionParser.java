/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import org.w3c.dom.Element;

/**
 * Parser for the <code>&lt;sws:marshalling-endpoints/&gt; element.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
class MarshallingEndpointsBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    private static final String GENERIC_MARSHALLING_METHOD_ENDPOINT_ADAPTER_CLASS_NAME =
            "org.springframework.ws.server.endpoint.adapter.GenericMarshallingMethodEndpointAdapter";

    private static final boolean genericAdapterPresent =
            ClassUtils.isPresent(GENERIC_MARSHALLING_METHOD_ENDPOINT_ADAPTER_CLASS_NAME,
                    MarshallingEndpointsBeanDefinitionParser.class.getClassLoader());

    private static final String MARSHALLING_METHOD_ENDPOINT_ADAPTER_CLASS_NAME =
            "org.springframework.ws.server.endpoint.adapter.MarshallingMethodEndpointAdapter";

    @Override
    protected boolean shouldGenerateIdAsFallback() {
        return true;
    }

    @Override
    protected String getBeanClassName(Element element) {
        if (genericAdapterPresent) {
                return GENERIC_MARSHALLING_METHOD_ENDPOINT_ADAPTER_CLASS_NAME;
        }
        return MARSHALLING_METHOD_ENDPOINT_ADAPTER_CLASS_NAME;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder beanDefinitionBuilder) {
        String marshallerName = element.getAttribute("marshaller");
        if (StringUtils.hasText(marshallerName)) {
            beanDefinitionBuilder.addPropertyReference("marshaller", marshallerName);
        }
        String unmarshallerName = element.getAttribute("unmarshaller");
        if (StringUtils.hasText(unmarshallerName)) {
            beanDefinitionBuilder.addPropertyReference("unmarshaller", unmarshallerName);
        }
    }

}
