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
import java.io.InputStream;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.wsdl.WsdlDefinition;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class WsdlDefinitionHandlerAdapterTest {

	private WsdlDefinitionHandlerAdapter adapter;

	private WsdlDefinition definitionMock;

	private MockHttpServletRequest request;

	private MockHttpServletResponse response;

	@BeforeEach
	public void setUp() throws Exception {

		this.adapter = new WsdlDefinitionHandlerAdapter();
		this.definitionMock = createMock(WsdlDefinition.class);
		this.adapter.afterPropertiesSet();
		this.request = new MockHttpServletRequest();
		this.response = new MockHttpServletResponse();
	}

	@Test
	public void handleGet() throws Exception {

		this.request.setMethod(HttpTransportConstants.METHOD_GET);
		String definition = "<definition xmlns='http://schemas.xmlsoap.org/wsdl/'/>";
		expect(this.definitionMock.getSource()).andReturn(new StringSource(definition));

		replay(this.definitionMock);

		this.adapter.handle(this.request, this.response, this.definitionMock);

		XmlAssert.assertThat(this.response.getContentAsString()).and(definition).ignoreWhitespace().areIdentical();

		verify(this.definitionMock);
	}

	@Test
	public void handleGetUpToDate() throws Exception {
		this.request.setMethod(HttpTransportConstants.METHOD_GET);
		Resource single = new ClassPathResource("echo-input.wsdl", getClass());
		long lastModified = single.getFile().lastModified();
		SimpleWsdl11Definition definition = new SimpleWsdl11Definition(single);
		definition.afterPropertiesSet();
		this.request.addHeader(HttpHeaders.IF_MODIFIED_SINCE, lastModified);
		this.adapter.handle(this.request, this.response, definition);
		assertThat(this.response.getStatus()).isEqualTo(HttpStatus.NOT_MODIFIED.value());
		assertThat(this.response.getContentLength()).isEqualTo(0);
	}

	@Test
	public void handleGetNotUpToDate() throws Exception {
		this.request.setMethod(HttpTransportConstants.METHOD_GET);
		Resource single = new ClassPathResource("echo-input.wsdl", getClass());
		long lastModified = single.getFile().lastModified();
		SimpleWsdl11Definition definition = new SimpleWsdl11Definition(single);
		definition.afterPropertiesSet();
		this.request.addHeader(HttpHeaders.IF_MODIFIED_SINCE, lastModified - 10000);
		this.adapter.handle(this.request, this.response, definition);
		assertThat(this.response.getStatus()).isEqualTo(HttpStatus.OK.value());
		String expected = new String(FileCopyUtils.copyToByteArray(single.getFile()));
		XmlAssert.assertThat(this.response.getContentAsString()).and(expected).ignoreWhitespace().areIdentical();
	}

	@Test
	public void handleNonGet() throws Exception {

		this.request.setMethod(HttpTransportConstants.METHOD_POST);

		replay(this.definitionMock);

		this.adapter.handle(this.request, this.response, this.definitionMock);

		assertThat(this.response.getStatus()).isEqualTo(HttpServletResponse.SC_METHOD_NOT_ALLOWED);

		verify(this.definitionMock);
	}

	@Test
	public void transformLocations() throws Exception {

		this.adapter.setTransformLocations(true);
		this.request.setMethod(HttpTransportConstants.METHOD_GET);
		this.request.setScheme("http");
		this.request.setServerName("example.com");
		this.request.setServerPort(8080);
		this.request.setContextPath("/context");
		this.request.setServletPath("/service.wsdl");
		this.request.setPathInfo(null);
		this.request.setRequestURI("/context/service.wsdl");

		replay(this.definitionMock);

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document result = documentBuilder.parse(getClass().getResourceAsStream("wsdl11-input.wsdl"));
		this.adapter.transformLocations(result, this.request);
		Document expectedDocument = documentBuilder.parse(getClass().getResourceAsStream("wsdl11-expected.wsdl"));

		XmlAssert.assertThat(result).and(expectedDocument).ignoreWhitespace().areIdentical();

		verify(this.definitionMock);
	}

	@Test
	public void transformLocationFullUrl() throws Exception {

		this.request.setScheme("http");
		this.request.setServerName("example.com");
		this.request.setServerPort(8080);
		this.request.setContextPath("/context");
		this.request.setPathInfo("/service.wsdl");
		this.request.setRequestURI("/context/service.wsdl");
		String oldLocation = "http://localhost:8080/context/service";

		String result = this.adapter.transformLocation(oldLocation, this.request);

		assertThat(result).isNotNull();
		assertThat(new URI(result)).isEqualTo(new URI("http://example.com:8080/context/service"));
	}

	@Test
	public void transformLocationEmptyContextFullUrl() throws Exception {

		this.request.setScheme("http");
		this.request.setServerName("example.com");
		this.request.setServerPort(8080);
		this.request.setContextPath("");
		this.request.setRequestURI("/service.wsdl");
		String oldLocation = "http://localhost:8080/service";

		String result = this.adapter.transformLocation(oldLocation, this.request);

		assertThat(result).isNotNull();
		assertThat(new URI(result)).isEqualTo(new URI("http://example.com:8080/service"));
	}

	@Test
	public void transformLocationRelativeUrl() throws Exception {

		this.request.setScheme("http");
		this.request.setServerName("example.com");
		this.request.setServerPort(8080);
		this.request.setContextPath("/context");
		this.request.setPathInfo("/service.wsdl");
		this.request.setRequestURI("/context/service.wsdl");
		String oldLocation = "/service";

		String result = this.adapter.transformLocation(oldLocation, this.request);

		assertThat(result).isNotNull();
		assertThat(new URI(result)).isEqualTo(new URI("http://example.com:8080/context/service"));
	}

	@Test
	public void transformLocationEmptyContextRelativeUrl() throws Exception {

		this.request.setScheme("http");
		this.request.setServerName("example.com");
		this.request.setServerPort(8080);
		this.request.setContextPath("");
		this.request.setRequestURI("/service.wsdl");
		String oldLocation = "/service";

		String result = this.adapter.transformLocation(oldLocation, this.request);

		assertThat(result).isNotNull();
		assertThat(new URI(result)).isEqualTo(new URI("http://example.com:8080/service"));
	}

	@Test
	public void handleSimpleWsdl11DefinitionWithoutTransformLocations() throws Exception {

		this.adapter.setTransformLocations(false);
		this.request.setMethod(HttpTransportConstants.METHOD_GET);
		this.request.setScheme("http");
		this.request.setServerName("example.com");
		this.request.setServerPort(8080);
		this.request.setContextPath("/context");
		this.request.setServletPath("/service.wsdl");
		this.request.setPathInfo(null);
		this.request.setRequestURI("/context/service.wsdl");

		SimpleWsdl11Definition definition = new SimpleWsdl11Definition(
				new ClassPathResource("echo-input.wsdl", getClass()));

		this.adapter.handle(this.request, this.response, definition);

		InputStream inputStream = new ByteArrayInputStream(this.response.getContentAsByteArray());
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document resultingDocument = documentBuilder.parse(inputStream);

		documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document expectedDocument = documentBuilder.parse(getClass().getResourceAsStream("echo-input.wsdl"));

		XmlAssert.assertThat(resultingDocument).and(expectedDocument).ignoreWhitespace().areIdentical();
	}

	@Test
	public void handleSimpleWsdl11DefinitionWithTransformLocation() throws Exception {

		this.adapter.setTransformLocations(true);
		this.adapter.setTransformSchemaLocations(true);

		this.request.setMethod(HttpTransportConstants.METHOD_GET);
		this.request.setScheme("http");
		this.request.setServerName("example.com");
		this.request.setServerPort(80);
		this.request.setContextPath("/context");
		this.request.setServletPath("/service.wsdl");
		this.request.setPathInfo(null);
		this.request.setRequestURI("/context/service.wsdl");

		SimpleWsdl11Definition definition = new SimpleWsdl11Definition(
				new ClassPathResource("echo-input.wsdl", getClass()));

		this.adapter.handle(this.request, this.response, definition);

		InputStream inputStream = new ByteArrayInputStream(this.response.getContentAsByteArray());
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document resultingDocument = documentBuilder.parse(inputStream);

		documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document expectedDocument = documentBuilder.parse(getClass().getResourceAsStream("echo-expected.wsdl"));

		XmlAssert.assertThat(resultingDocument).and(expectedDocument).ignoreWhitespace().areIdentical();
	}

	@Test
	public void handlesForwardedHeadersInRequest() {

		// given
		this.request.setScheme("http");
		this.request.setServerName("example.com");
		this.request.setServerPort(80);
		this.request.setContextPath("/context");
		this.request.setPathInfo("/service.wsdl");

		this.request.addHeader("X-Forwarded-Proto", "https");
		this.request.addHeader("X-Forwarded-Host", "loadbalancer.com");
		this.request.addHeader("X-Forwarded-Port", "8080");

		// when
		String result = this.adapter.transformLocation("/service", this.request);

		// then
		assertThat(URI.create("https://loadbalancer.com:8080/context/service")).isEqualTo(URI.create(result));
	}

	@Test
	public void handlesNoForwardedHeadersInRequest() {

		// given
		this.request.setScheme("http");
		this.request.setServerName("example.com");
		this.request.setServerPort(80);
		this.request.setContextPath("/context");
		this.request.setPathInfo("/service.wsdl");

		// when
		String result = this.adapter.transformLocation("/service", this.request);

		// then
		assertThat(URI.create("http://example.com:80/context/service")).isEqualTo(URI.create(result));
	}

}
