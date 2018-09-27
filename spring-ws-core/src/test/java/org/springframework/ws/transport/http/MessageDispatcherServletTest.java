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

package org.springframework.ws.transport.http;

import java.io.ByteArrayInputStream;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.ws.server.MessageDispatcher;
import org.springframework.ws.server.endpoint.adapter.PayloadEndpointAdapter;
import org.springframework.ws.server.endpoint.mapping.PayloadRootQNameEndpointMapping;
import org.springframework.ws.soap.server.endpoint.SimpleSoapExceptionResolver;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.xmlunit.assertj.XmlAssert.assertThat;

public class MessageDispatcherServletTest {

	private ServletConfig config;

	private MessageDispatcherServlet servlet;

	@Before
	public void setUp() throws Exception {
		config = new MockServletConfig(new MockServletContext(), "spring-ws");
		servlet = new MessageDispatcherServlet();
	}

	private void assertStrategies(Class<?> expectedClass, List<?> actual) {
		Assert.assertEquals("Invalid amount of strategies", 1, actual.size());
		Object strategy = actual.get(0);
		Assert.assertTrue("Invalid strategy", expectedClass.isAssignableFrom(strategy.getClass()));
	}

	@Test
	public void testDefaultStrategies() throws ServletException {
		servlet.setContextClass(StaticWebApplicationContext.class);
		servlet.init(config);
		MessageDispatcher messageDispatcher = (MessageDispatcher) servlet.getMessageReceiver();
		Assert.assertNotNull("No messageDispatcher created", messageDispatcher);
	}

	@Test
	public void testDetectedStrategies() throws ServletException {
		servlet.setContextClass(DetectWebApplicationContext.class);
		servlet.init(config);
		MessageDispatcher messageDispatcher = (MessageDispatcher) servlet.getMessageReceiver();
		Assert.assertNotNull("No messageDispatcher created", messageDispatcher);
		assertStrategies(PayloadRootQNameEndpointMapping.class, messageDispatcher.getEndpointMappings());
		assertStrategies(PayloadEndpointAdapter.class, messageDispatcher.getEndpointAdapters());
		assertStrategies(SimpleSoapExceptionResolver.class, messageDispatcher.getEndpointExceptionResolvers());
	}

	@Test
	public void testDetectWsdlDefinitions() throws Exception {
		servlet.setContextClass(WsdlDefinitionWebApplicationContext.class);
		servlet.init(config);
		MockHttpServletRequest request =
				new MockHttpServletRequest(HttpTransportConstants.METHOD_GET, "/definition.wsdl");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document result = documentBuilder.parse(new ByteArrayInputStream(response.getContentAsByteArray()));
		Document expected = documentBuilder.parse(getClass().getResourceAsStream("wsdl11-input.wsdl"));
		assertThat(result).and(expected).areSimilar();
	}

	private static class DetectWebApplicationContext extends StaticWebApplicationContext {

		@Override
		public void refresh() throws BeansException, IllegalStateException {
			registerSingleton("payloadMapping", PayloadRootQNameEndpointMapping.class);
			registerSingleton("payloadAdapter", PayloadEndpointAdapter.class);
			registerSingleton("simpleExceptionResolver", SimpleSoapExceptionResolver.class);
			super.refresh();
		}
	}

	private static class WsdlDefinitionWebApplicationContext extends StaticWebApplicationContext {

		@Override
		public void refresh() throws BeansException, IllegalStateException {
			MutablePropertyValues mpv = new MutablePropertyValues();
			mpv.addPropertyValue("wsdl", new ClassPathResource("wsdl11-input.wsdl", getClass()));
			registerSingleton("definition", SimpleWsdl11Definition.class, mpv);
			super.refresh();
		}
	}
}