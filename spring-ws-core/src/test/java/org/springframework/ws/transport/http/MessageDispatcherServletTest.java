/*
 * Copyright 2005-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xmlunit.assertj.XmlAssert;

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
import org.springframework.xml.DocumentBuilderFactoryUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageDispatcherServletTest {

	private ServletConfig config;

	private MessageDispatcherServlet servlet;

	@BeforeEach
	public void setUp() {

		this.config = new MockServletConfig(new MockServletContext(), "spring-ws");
		this.servlet = new MessageDispatcherServlet();
	}

	private void assertStrategies(Class<?> expectedClass, List<?> actual) {

		assertThat(actual).hasSize(1);

		Object strategy = actual.get(0);

		assertThat(expectedClass).isAssignableFrom(strategy.getClass());
	}

	@Test
	public void testDefaultStrategies() throws ServletException {

		this.servlet.setContextClass(StaticWebApplicationContext.class);
		this.servlet.init(this.config);
		MessageDispatcher messageDispatcher = (MessageDispatcher) this.servlet.getMessageReceiver();

		assertThat(messageDispatcher).isNotNull();
	}

	@Test
	public void testDetectedStrategies() throws ServletException {

		this.servlet.setContextClass(DetectWebApplicationContext.class);
		this.servlet.init(this.config);
		MessageDispatcher messageDispatcher = (MessageDispatcher) this.servlet.getMessageReceiver();

		assertThat(messageDispatcher).isNotNull();
		assertStrategies(PayloadRootQNameEndpointMapping.class, messageDispatcher.getEndpointMappings());
		assertStrategies(PayloadEndpointAdapter.class, messageDispatcher.getEndpointAdapters());
		assertStrategies(SimpleSoapExceptionResolver.class, messageDispatcher.getEndpointExceptionResolvers());
	}

	@Test
	public void testDetectWsdlDefinitions() throws Exception {

		this.servlet.setContextClass(WsdlDefinitionWebApplicationContext.class);
		this.servlet.init(this.config);
		MockHttpServletRequest request = new MockHttpServletRequest(HttpTransportConstants.METHOD_GET,
				"/definition.wsdl");
		MockHttpServletResponse response = new MockHttpServletResponse();
		this.servlet.service(request, response);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document result = documentBuilder.parse(new ByteArrayInputStream(response.getContentAsByteArray()));
		Document expected = documentBuilder.parse(getClass().getResourceAsStream("wsdl11-input.wsdl"));

		XmlAssert.assertThat(result).and(expected).ignoreWhitespace().areIdentical();
	}

	private static final class DetectWebApplicationContext extends StaticWebApplicationContext {

		@Override
		public void refresh() throws BeansException, IllegalStateException {

			registerSingleton("payloadMapping", PayloadRootQNameEndpointMapping.class);
			registerSingleton("payloadAdapter", PayloadEndpointAdapter.class);
			registerSingleton("simpleExceptionResolver", SimpleSoapExceptionResolver.class);

			super.refresh();
		}

	}

	private static final class WsdlDefinitionWebApplicationContext extends StaticWebApplicationContext {

		@Override
		public void refresh() throws BeansException, IllegalStateException {

			MutablePropertyValues mpv = new MutablePropertyValues();
			mpv.addPropertyValue("wsdl", new ClassPathResource("wsdl11-input.wsdl", getClass()));
			registerSingleton("definition", SimpleWsdl11Definition.class, mpv);

			super.refresh();
		}

	}

}
