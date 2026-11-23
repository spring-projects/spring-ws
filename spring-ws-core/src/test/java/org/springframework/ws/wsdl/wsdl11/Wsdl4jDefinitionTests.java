/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.wsdl.wsdl11;

import java.io.InputStream;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.transform.TransformerFactoryUtils;

import static org.assertj.core.api.Assertions.assertThat;

class Wsdl4jDefinitionTests {

	private Wsdl4jDefinition definition;

	private Transformer transformer;

	@BeforeEach
	void setUp() throws Exception {

		WSDLFactory factory = WSDLFactory.newInstance();
		WSDLReader reader = factory.newWSDLReader();

		try (InputStream is = getClass().getResourceAsStream("complete.wsdl")) {
			Definition wsdl4jDefinition = reader.readWSDL(null, new InputSource(is));
			this.definition = new Wsdl4jDefinition(wsdl4jDefinition);
		}

		this.transformer = TransformerFactoryUtils.newInstance().newTransformer();
	}

	@Test
	void testGetSource() throws Exception {

		Source source = this.definition.getSource();

		assertThat(source).isNotNull();

		DOMResult result = new DOMResult();
		this.transformer.transform(source, result);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document expected = documentBuilder.parse(getClass().getResourceAsStream("complete.wsdl"));

		XmlAssert.assertThat(result.getNode()).and(expected).ignoreWhitespace().areIdentical();
	}

}
