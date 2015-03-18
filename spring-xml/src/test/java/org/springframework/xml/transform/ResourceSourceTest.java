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

package org.springframework.xml.transform;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.springframework.core.io.ClassPathResource;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;

public class ResourceSourceTest {

	@Test
	public void testStringSource() throws Exception {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		DOMResult result = new DOMResult();
		ResourceSource source = new ResourceSource(new ClassPathResource("resourceSource.xml", getClass()));
		transformer.transform(source, result);
		Element rootElement = (Element) result.getNode().getFirstChild();
		Assert.assertEquals("Invalid local name", "content", rootElement.getLocalName());
		Assert.assertEquals("Invalid prefix", "prefix", rootElement.getPrefix());
		Assert.assertEquals("Invalid namespace", "namespace", rootElement.getNamespaceURI());
	}

}