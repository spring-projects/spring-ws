/*
 * Copyright 2005-present the original author or authors.
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
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.xsd.SimpleXsdSchema;

import static org.assertj.core.api.Assertions.assertThat;

class XsdSchemaHandlerAdapterTests {

	private XsdSchemaHandlerAdapter adapter;

	private MockHttpServletRequest request;

	private MockHttpServletResponse response;

	@BeforeEach
	void setUp() throws Exception {

		this.adapter = new XsdSchemaHandlerAdapter();
		this.adapter.afterPropertiesSet();
		this.request = new MockHttpServletRequest();
		this.response = new MockHttpServletResponse();
	}

	@Test
	@Deprecated
	void getLastModified() throws Exception {

		Resource single = new ClassPathResource("single.xsd", getClass());
		SimpleXsdSchema schema = new SimpleXsdSchema(single);
		schema.afterPropertiesSet();
		long lastModified = single.getFile().lastModified();

		assertThat(this.adapter.getLastModified(null, schema)).isEqualTo(lastModified);
	}

	@Test
	void handleGet() throws Exception {

		this.request.setMethod(HttpTransportConstants.METHOD_GET);
		Resource single = new ClassPathResource("single.xsd", getClass());
		SimpleXsdSchema schema = new SimpleXsdSchema(single);
		schema.afterPropertiesSet();
		this.adapter.handle(this.request, this.response, schema);
		String expected = new String(FileCopyUtils.copyToByteArray(single.getFile()));

		XmlAssert.assertThat(this.response.getContentAsString()).and(expected).ignoreWhitespace().areIdentical();
	}

	@Test
	void handleGetUpToDate() throws Exception {
		this.request.setMethod(HttpTransportConstants.METHOD_GET);
		Resource single = new ClassPathResource("single.xsd", getClass());
		long lastModified = single.getFile().lastModified();
		SimpleXsdSchema schema = new SimpleXsdSchema(single);
		schema.afterPropertiesSet();
		this.request.addHeader(HttpHeaders.IF_MODIFIED_SINCE, lastModified);
		this.adapter.handle(this.request, this.response, schema);
		assertThat(this.response.getStatus()).isEqualTo(HttpStatus.NOT_MODIFIED.value());
		assertThat(this.response.getContentLength()).isEqualTo(0);
	}

	@Test
	void handleGetNotUpToDate() throws Exception {
		this.request.setMethod(HttpTransportConstants.METHOD_GET);
		Resource single = new ClassPathResource("single.xsd", getClass());
		long lastModified = single.getFile().lastModified();
		SimpleXsdSchema schema = new SimpleXsdSchema(single);
		schema.afterPropertiesSet();
		this.request.addHeader(HttpHeaders.IF_MODIFIED_SINCE, lastModified - 1000);
		this.adapter.handle(this.request, this.response, schema);
		assertThat(this.response.getStatus()).isEqualTo(HttpStatus.OK.value());
		String expected = new String(FileCopyUtils.copyToByteArray(single.getFile()));
		XmlAssert.assertThat(this.response.getContentAsString()).and(expected).ignoreWhitespace().areIdentical();
	}

	@Test
	void handleNonGet() throws Exception {

		this.request.setMethod(HttpTransportConstants.METHOD_POST);
		this.adapter.handle(this.request, this.response, null);

		assertThat(this.response.getStatus()).isEqualTo(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	@Test
	void handleGetWithTransformLocation() throws Exception {

		this.adapter.setTransformSchemaLocations(true);

		this.request.setMethod(HttpTransportConstants.METHOD_GET);
		this.request.setScheme("http");
		this.request.setServerName("example.com");
		this.request.setServerPort(80);
		this.request.setContextPath("/context");
		this.request.setServletPath("/service.xsd");
		this.request.setPathInfo(null);
		this.request.setRequestURI("/context/service.xsd");

		Resource importing = new ClassPathResource("importing-input.xsd", getClass());
		SimpleXsdSchema schema = new SimpleXsdSchema(importing);
		schema.afterPropertiesSet();

		this.adapter.handle(this.request, this.response, schema);

		InputStream inputStream = new ByteArrayInputStream(this.response.getContentAsByteArray());
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document resultingDocument = documentBuilder.parse(inputStream);

		documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document expectedDocument = documentBuilder.parse(getClass().getResourceAsStream("importing-expected.xsd"));

		XmlAssert.assertThat(resultingDocument).and(expectedDocument).ignoreWhitespace().areIdentical();
	}

}
