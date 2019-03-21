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

package org.springframework.ws.soap;

import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.springframework.xml.transform.TransformerFactoryUtils;

public abstract class AbstractSoapElementTestCase {

	private SoapElement soapElement;

	protected Transformer transformer;

	@Before
	public final void setUp() throws Exception {
		TransformerFactory transformerFactory = TransformerFactoryUtils.newInstance();
		transformer = transformerFactory.newTransformer();
		soapElement = createSoapElement();
	}

	protected abstract SoapElement createSoapElement() throws Exception;

	@Test
	public void testAttributes() throws Exception {
		QName name = new QName("http://springframework.org/spring-ws", "attribute");
		String value = "value";
		soapElement.addAttribute(name, value);
		Assert.assertEquals("Invalid attribute value", value, soapElement.getAttributeValue(name));
		Iterator<QName> allAttributes = soapElement.getAllAttributes();
		Assert.assertTrue("Iterator is empty", allAttributes.hasNext());
	}

	@Test
	public void testAddNamespaceDeclaration() throws Exception {
		String prefix = "p";
		String namespace = "http://springframework.org/spring-ws";
		soapElement.addNamespaceDeclaration(prefix, namespace);
	}

	@Test
	public void testAddDefaultNamespaceDeclaration() throws Exception {
		String prefix = "";
		String namespace = "http://springframework.org/spring-ws";
		soapElement.addNamespaceDeclaration(prefix, namespace);
	}


}
