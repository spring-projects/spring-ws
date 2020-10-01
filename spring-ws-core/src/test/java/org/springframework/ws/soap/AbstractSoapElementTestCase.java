/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap;

import static org.assertj.core.api.Assertions.*;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.xml.transform.TransformerFactoryUtils;

public abstract class AbstractSoapElementTestCase {

	private SoapElement soapElement;

	protected Transformer transformer;

	@BeforeEach
	public final void setUp() throws Exception {

		TransformerFactory transformerFactory = TransformerFactoryUtils.newInstance();
		transformer = transformerFactory.newTransformer();
		soapElement = createSoapElement();
	}

	protected abstract SoapElement createSoapElement() throws Exception;

	@Test
	public void testAttributes() {

		QName name = new QName("http://springframework.org/spring-ws", "attribute");
		String value = "value";
		soapElement.addAttribute(name, value);

		assertThat(soapElement.getAttributeValue(name)).isEqualTo(value);

		Iterator<QName> allAttributes = soapElement.getAllAttributes();

		assertThat(allAttributes.hasNext()).isTrue();
	}

	@Test
	public void testAddNamespaceDeclaration() {

		String prefix = "p";
		String namespace = "http://springframework.org/spring-ws";
		soapElement.addNamespaceDeclaration(prefix, namespace);
	}

	@Test
	public void testAddDefaultNamespaceDeclaration() {

		String prefix = "";
		String namespace = "http://springframework.org/spring-ws";
		soapElement.addNamespaceDeclaration(prefix, namespace);
	}
}
