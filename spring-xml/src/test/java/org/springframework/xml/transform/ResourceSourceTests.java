/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.transform;

import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceSourceTests {

	@Test
	void testStringSource() throws Exception {

		Transformer transformer = TransformerFactoryUtils.newInstance().newTransformer();
		DOMResult result = new DOMResult();
		ResourceSource source = new ResourceSource(new ClassPathResource("resourceSource.xml", getClass()));
		transformer.transform(source, result);
		Element rootElement = (Element) result.getNode().getFirstChild();

		assertThat(rootElement.getLocalName()).isEqualTo("content");
		assertThat(rootElement.getPrefix()).isEqualTo("prefix");
		assertThat(rootElement.getNamespaceURI()).isEqualTo("namespace");
	}

}
