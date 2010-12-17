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
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.springframework.ws.server.SmartEndpointInterceptor;
import org.springframework.ws.server.endpoint.interceptor.DelegatingSmartEndpointInterceptor;
import org.springframework.ws.server.endpoint.interceptor.PayloadRootSmartEndpointInterceptor;
import org.springframework.ws.soap.server.endpoint.interceptor.SoapActionSmartEndpointInterceptor;

import org.w3c.dom.Element;

/**
 * Parser for the {@code &lt;sws:interceptors/&gt;} element.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
class InterceptorsBeanDefinitionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        CompositeComponentDefinition compDefinition =
                new CompositeComponentDefinition(element.getTagName(), parserContext.extractSource(element));
        parserContext.pushContainingComponent(compDefinition);

        List<Element> childElements = DomUtils.getChildElements(element);
        for (Element childElement : childElements) {
            if ("bean".equals(childElement.getLocalName())) {
                RootBeanDefinition smartInterceptorDef =
                        createSmartInterceptorDefinition(DelegatingSmartEndpointInterceptor.class, childElement,
                                parserContext);
                BeanDefinitionHolder interceptorDef = createInterceptorDefinition(parserContext, childElement);

                smartInterceptorDef.getConstructorArgumentValues().addIndexedArgumentValue(0, interceptorDef);

                registerSmartInterceptor(parserContext, smartInterceptorDef);
            }
            else if ("payloadRoot".equals(childElement.getLocalName())) {
                List<Element> beanElements = DomUtils.getChildElementsByTagName(childElement, "bean");
                for (Element beanElement : beanElements) {
                    RootBeanDefinition smartInterceptorDef =
                            createSmartInterceptorDefinition(PayloadRootSmartEndpointInterceptor.class, childElement,
                                    parserContext);
                    BeanDefinitionHolder interceptorDef = createInterceptorDefinition(parserContext, beanElement);

                    String namespaceUri = childElement.getAttribute("namespaceUri");
                    String localPart = childElement.getAttribute("localPart");

                    smartInterceptorDef.getConstructorArgumentValues().addIndexedArgumentValue(0, interceptorDef);
                    smartInterceptorDef.getConstructorArgumentValues().addIndexedArgumentValue(1, namespaceUri);
                    smartInterceptorDef.getConstructorArgumentValues().addIndexedArgumentValue(2, localPart);

                    registerSmartInterceptor(parserContext, smartInterceptorDef);
                }
            }
            else if ("soapAction".equals(childElement.getLocalName())) {
                List<Element> beanElements = DomUtils.getChildElementsByTagName(childElement, "bean");
                for (Element beanElement : beanElements) {
                    RootBeanDefinition smartInterceptorDef =
                            createSmartInterceptorDefinition(SoapActionSmartEndpointInterceptor.class, childElement,
                                    parserContext);
                    BeanDefinitionHolder interceptorDef = createInterceptorDefinition(parserContext, beanElement);

                    String soapAction = childElement.getAttribute("value");

                    smartInterceptorDef.getConstructorArgumentValues().addIndexedArgumentValue(0, interceptorDef);
                    smartInterceptorDef.getConstructorArgumentValues().addIndexedArgumentValue(1, soapAction);

                    registerSmartInterceptor(parserContext, smartInterceptorDef);
                }
            }
        }

        parserContext.popAndRegisterContainingComponent();
        return null;
    }

    private void registerSmartInterceptor(ParserContext parserContext, RootBeanDefinition smartInterceptorDef) {
        String mappedInterceptorName =
                parserContext.getReaderContext().registerWithGeneratedName(smartInterceptorDef);
        parserContext
                .registerComponent(new BeanComponentDefinition(smartInterceptorDef, mappedInterceptorName));
    }

    private BeanDefinitionHolder createInterceptorDefinition(ParserContext parserContext, Element element) {
        BeanDefinitionHolder interceptorDef =
                parserContext.getDelegate().parseBeanDefinitionElement(element);
        interceptorDef =
                parserContext.getDelegate().decorateBeanDefinitionIfRequired(element, interceptorDef);
        return interceptorDef;
    }

    private RootBeanDefinition createSmartInterceptorDefinition(Class<? extends SmartEndpointInterceptor> interceptorClass,
                                                                Element element,
                                                                ParserContext parserContext) {
        RootBeanDefinition smartInterceptorDef = new RootBeanDefinition(interceptorClass);
        smartInterceptorDef.setSource(parserContext.extractSource(element));
        smartInterceptorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        return smartInterceptorDef;
    }

}
