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

import static org.xmlunit.assertj.XmlAssert.*;

import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;

import org.junit.jupiter.api.Test;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class StringResultTest {

	@Test
	public void testStringResult() throws Exception {

		Document document = DocumentBuilderFactoryUtils.newInstance().newDocumentBuilder().newDocument();
		Element element = document.createElementNS("namespace", "prefix:localName");
		document.appendChild(element);

		Transformer transformer = TransformerFactoryUtils.newInstance().newTransformer();
		StringResult result = new StringResult();
		transformer.transform(new DOMSource(document), result);

		assertThat(result.toString()).and("<prefix:localName xmlns:prefix='namespace'/>").ignoreWhitespace().areIdentical();
	}

}
