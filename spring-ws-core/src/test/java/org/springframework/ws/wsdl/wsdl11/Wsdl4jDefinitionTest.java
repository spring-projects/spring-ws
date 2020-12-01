/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.wsdl.wsdl11;

import static org.assertj.core.api.Assertions.*;

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
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.transform.TransformerFactoryUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xmlunit.assertj.XmlAssert;

public class Wsdl4jDefinitionTest {

	private Wsdl4jDefinition definition;

	private Transformer transformer;

	@BeforeEach
	public void setUp() throws Exception {

		WSDLFactory factory = WSDLFactory.newInstance();
		WSDLReader reader = factory.newWSDLReader();
		InputStream is = getClass().getResourceAsStream("complete.wsdl");

		try {
			Definition wsdl4jDefinition = reader.readWSDL(null, new InputSource(is));
			definition = new Wsdl4jDefinition(wsdl4jDefinition);
		} finally {
			is.close();
		}

		transformer = TransformerFactoryUtils.newInstance().newTransformer();
	}

	@Test
	public void testGetSource() throws Exception {

		Source source = definition.getSource();

		assertThat(source).isNotNull();

		DOMResult result = new DOMResult();
		transformer.transform(source, result);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document expected = documentBuilder.parse(getClass().getResourceAsStream("complete.wsdl"));

		XmlAssert.assertThat(result.getNode()).and(expected).ignoreWhitespace().areIdentical();
	}
}
