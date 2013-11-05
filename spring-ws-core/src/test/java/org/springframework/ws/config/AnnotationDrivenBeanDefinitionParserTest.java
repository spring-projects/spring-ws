/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ws.server.EndpointAdapter;
import org.springframework.ws.server.EndpointExceptionResolver;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.adapter.method.MessageContextMethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;
import org.springframework.ws.server.endpoint.adapter.method.SourcePayloadMethodProcessor;
import org.springframework.ws.server.endpoint.adapter.method.StaxPayloadMethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.XPathParamMethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.dom.Dom4jPayloadMethodProcessor;
import org.springframework.ws.server.endpoint.adapter.method.dom.DomPayloadMethodProcessor;
import org.springframework.ws.server.endpoint.adapter.method.dom.JDomPayloadMethodProcessor;
import org.springframework.ws.server.endpoint.adapter.method.dom.XomPayloadMethodProcessor;
import org.springframework.ws.server.endpoint.adapter.method.jaxb.JaxbElementPayloadMethodProcessor;
import org.springframework.ws.server.endpoint.adapter.method.jaxb.XmlRootElementPayloadMethodProcessor;
import org.springframework.ws.server.endpoint.mapping.PayloadRootAnnotationMethodEndpointMapping;
import org.springframework.ws.soap.addressing.server.AnnotationActionEndpointMapping;
import org.springframework.ws.soap.server.endpoint.SimpleSoapExceptionResolver;
import org.springframework.ws.soap.server.endpoint.SoapFaultAnnotationExceptionResolver;
import org.springframework.ws.soap.server.endpoint.adapter.method.SoapHeaderElementMethodArgumentResolver;
import org.springframework.ws.soap.server.endpoint.adapter.method.SoapMethodArgumentResolver;
import org.springframework.ws.soap.server.endpoint.mapping.SoapActionAnnotationMethodEndpointMapping;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Arjen Poutsma
 */
public class AnnotationDrivenBeanDefinitionParserTest {

    private ApplicationContext applicationContext;

    @Before
    public void setUp() throws Exception {
        applicationContext =
                new ClassPathXmlApplicationContext("annotationDrivenBeanDefinitionParserTest.xml", getClass());
    }

    @Test
    public void endpointMappings() {
        Map<String, EndpointMapping> result = applicationContext.getBeansOfType(EndpointMapping.class);
        assertEquals("invalid amount of endpoint mappings found", 3, result.size());
        assertContainsInstanceOf(result.values(), PayloadRootAnnotationMethodEndpointMapping.class);
        assertContainsInstanceOf(result.values(), SoapActionAnnotationMethodEndpointMapping.class);
        assertContainsInstanceOf(result.values(), AnnotationActionEndpointMapping.class);
    }

    @Test
    public void endpointAdapters() {
        Map<String, EndpointAdapter> result =
                applicationContext.getBeansOfType(EndpointAdapter.class);
        assertEquals("invalid amount of endpoint mappings found", 1, result.size());
        DefaultMethodEndpointAdapter endpointAdapter = (DefaultMethodEndpointAdapter) result.values().iterator().next();

        List<MethodArgumentResolver> argumentResolvers = endpointAdapter.getMethodArgumentResolvers();
        assertTrue("No argumentResolvers created", !argumentResolvers.isEmpty());
        assertContainsInstanceOf(argumentResolvers, MessageContextMethodArgumentResolver.class);
        assertContainsInstanceOf(argumentResolvers, XPathParamMethodArgumentResolver.class);
        assertContainsInstanceOf(argumentResolvers, SoapMethodArgumentResolver.class);
        assertContainsInstanceOf(argumentResolvers, SoapHeaderElementMethodArgumentResolver.class);
        assertContainsInstanceOf(argumentResolvers, DomPayloadMethodProcessor.class);
        assertContainsInstanceOf(argumentResolvers, SourcePayloadMethodProcessor.class);
        assertContainsInstanceOf(argumentResolvers, Dom4jPayloadMethodProcessor.class);
        assertContainsInstanceOf(argumentResolvers, XmlRootElementPayloadMethodProcessor.class);
        assertContainsInstanceOf(argumentResolvers, JaxbElementPayloadMethodProcessor.class);
        assertContainsInstanceOf(argumentResolvers, JDomPayloadMethodProcessor.class);
        assertContainsInstanceOf(argumentResolvers, StaxPayloadMethodArgumentResolver.class);
        assertContainsInstanceOf(argumentResolvers, XomPayloadMethodProcessor.class);

        List<MethodReturnValueHandler> returnValueHandlers = endpointAdapter.getMethodReturnValueHandlers();
        assertTrue("No returnValueHandlers created", !returnValueHandlers.isEmpty());
        assertContainsInstanceOf(returnValueHandlers, DomPayloadMethodProcessor.class);
        assertContainsInstanceOf(returnValueHandlers, SourcePayloadMethodProcessor.class);
        assertContainsInstanceOf(returnValueHandlers, Dom4jPayloadMethodProcessor.class);
        assertContainsInstanceOf(returnValueHandlers, XmlRootElementPayloadMethodProcessor.class);
        assertContainsInstanceOf(returnValueHandlers, JaxbElementPayloadMethodProcessor.class);
        assertContainsInstanceOf(returnValueHandlers, JDomPayloadMethodProcessor.class);
        assertContainsInstanceOf(returnValueHandlers, XomPayloadMethodProcessor.class);
    }

    @Test
    public void endpointExceptionResolver() {
        Map<String, EndpointExceptionResolver> result = applicationContext.getBeansOfType(EndpointExceptionResolver.class);
        assertEquals("invalid amount of endpoint exception resolvers found", 2, result.size());
        assertContainsInstanceOf(result.values(), SoapFaultAnnotationExceptionResolver.class);
        assertContainsInstanceOf(result.values(), SimpleSoapExceptionResolver.class);
    }


    private <T> void assertContainsInstanceOf(Collection<T> collection, Class<? extends T> clazz) {
        boolean found = false;
        for (T item : collection) {
            if (item.getClass().equals(clazz)) {
                found = true;
                break;
            }
        }
        assertTrue("No [" + clazz.getName() + "] instance found", found);
    }

}
