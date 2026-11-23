/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.transform;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import static org.assertj.core.api.Assertions.assertThat;

class StringSourceTests {

	@Test
	void testStringSource() throws TransformerException {

		Transformer transformer = TransformerFactoryUtils.newInstance().newTransformer();
		String content = "<prefix:content xmlns:prefix='namespace'/>";
		DOMResult result = new DOMResult();
		transformer.transform(new StringSource(content), result);
		Element rootElement = (Element) result.getNode().getFirstChild();

		assertThat(rootElement.getLocalName()).isEqualTo("content");
		assertThat(rootElement.getPrefix()).isEqualTo("prefix");
		assertThat(rootElement.getNamespaceURI()).isEqualTo("namespace");
	}

}
