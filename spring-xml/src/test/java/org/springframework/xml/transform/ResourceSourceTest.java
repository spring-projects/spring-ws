/*
 * Copyright 2005-2022 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;

import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Element;

public class ResourceSourceTest {

	@Test
	public void testStringSource() throws Exception {

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
