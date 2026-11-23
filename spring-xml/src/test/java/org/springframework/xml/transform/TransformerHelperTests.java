/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.transform;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.xmlunit.assertj.XmlAssert.assertThat;

class TransformerHelperTests {

	private TransformerHelper helper;

	private Transformer transformer;

	@BeforeEach
	void setUp() {

		this.helper = new TransformerHelper();
		this.transformer = mock(Transformer.class);
	}

	@Test
	void defaultTransformerFactory() throws TransformerException {

		String xml = "<root xmlns='http://springframework.org/spring-ws'><child>text</child></root>";
		Source source = new StringSource(xml);
		Result result = new StringResult();

		this.helper.transform(source, result);

		assertThat(result.toString()).and(xml).ignoreWhitespace().areIdentical();
	}

}
