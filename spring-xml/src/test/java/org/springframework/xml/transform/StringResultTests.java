/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.transform;

import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.springframework.xml.DocumentBuilderFactoryUtils;

import static org.xmlunit.assertj.XmlAssert.assertThat;

class StringResultTests {

	@Test
	void testStringResult() throws Exception {

		Document document = DocumentBuilderFactoryUtils.newInstance().newDocumentBuilder().newDocument();
		Element element = document.createElementNS("namespace", "prefix:localName");
		document.appendChild(element);

		Transformer transformer = TransformerFactoryUtils.newInstance().newTransformer();
		StringResult result = new StringResult();
		transformer.transform(new DOMSource(document), result);

		assertThat(result.toString()).and("<prefix:localName xmlns:prefix='namespace'/>")
			.ignoreWhitespace()
			.areIdentical();
	}

}
