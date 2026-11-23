/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.http;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.transform.ResourceSource;

import static org.assertj.core.api.Assertions.assertThat;

class LastModifiedHelperTests {

	private Resource resource;

	private long expected;

	@BeforeEach
	void setUp() throws Exception {

		this.resource = new ClassPathResource("single.xsd", getClass());
		this.expected = this.resource.lastModified();
	}

	@Test
	void testSaxSource() throws Exception {

		long result = LastModifiedHelper.getLastModified(new ResourceSource(this.resource));

		assertThat(result).isEqualTo(this.expected);
	}

	@Test
	void testDomSource() throws Exception {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(this.resource.getFile());
		long result = LastModifiedHelper.getLastModified(new DOMSource(document));

		assertThat(result).isEqualTo(this.expected);
	}

	@Test
	void testStreamSource() throws Exception {

		long result = LastModifiedHelper.getLastModified(new StreamSource(this.resource.getFile()));

		assertThat(result).isEqualTo(this.expected);
	}

}
