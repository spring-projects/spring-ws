/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
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
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.xml.xsd.SimpleXsdSchema;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.xmlunit.assertj.XmlAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class XsdSchemaHandlerAdapterTest {

	private XsdSchemaHandlerAdapter adapter;

	private MockHttpServletRequest request;

	private MockHttpServletResponse response;

	@Before
	public void setUp() throws Exception {
		adapter = new XsdSchemaHandlerAdapter();
		adapter.afterPropertiesSet();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@Test
	public void getLastModified() throws Exception {
		Resource single = new ClassPathResource("single.xsd", getClass());
		SimpleXsdSchema schema = new SimpleXsdSchema(single);
		schema.afterPropertiesSet();
		long lastModified = single.getFile().lastModified();
		assertEquals("Invalid last modified", lastModified, adapter.getLastModified(null, schema));
	}

	@Test
	public void handleGet() throws Exception {
		request.setMethod(HttpTransportConstants.METHOD_GET);
		Resource single = new ClassPathResource("single.xsd", getClass());
		SimpleXsdSchema schema = new SimpleXsdSchema(single);
		schema.afterPropertiesSet();
		adapter.handle(request, response, schema);
		String expected = new String(FileCopyUtils.copyToByteArray(single.getFile()));
		assertThat(response.getContentAsString()).and(expected).areSimilar();
	}

	@Test
	public void handleNonGet() throws Exception {
		request.setMethod(HttpTransportConstants.METHOD_POST);
		adapter.handle(request, response, null);
		assertEquals("METHOD_NOT_ALLOWED expected", HttpServletResponse.SC_METHOD_NOT_ALLOWED, response.getStatus());
	}

	@Test
	public void handleGetWithTransformLocation() throws Exception {
		adapter.setTransformSchemaLocations(true);

		request.setMethod(HttpTransportConstants.METHOD_GET);
		request.setScheme("http");
		request.setServerName("example.com");
		request.setServerPort(80);
		request.setContextPath("/context");
		request.setServletPath("/service.xsd");
		request.setPathInfo(null);
		request.setRequestURI("/context/service.xsd");

		Resource importing = new ClassPathResource("importing-input.xsd", getClass());
		SimpleXsdSchema schema = new SimpleXsdSchema(importing);
		schema.afterPropertiesSet();

		adapter.handle(request, response, schema);

		InputStream inputStream = new ByteArrayInputStream(response.getContentAsByteArray());
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document resultingDocument = documentBuilder.parse(inputStream);

		documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document expectedDocument = documentBuilder.parse(getClass().getResourceAsStream("importing-expected.xsd"));
		assertThat(resultingDocument).and(expectedDocument).areSimilar();
	}

}