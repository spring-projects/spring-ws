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

import static org.mockito.Mockito.*;
import static org.xmlunit.assertj.XmlAssert.*;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TransformerHelperTest {

	private TransformerHelper helper;
	private Transformer transformer;

	@BeforeEach
	public void setUp() throws Exception {

		helper = new TransformerHelper();
		transformer = mock(Transformer.class);
	}

	@Test
	public void defaultTransformerFactory() throws TransformerException {

		String xml = "<root xmlns='http://springframework.org/spring-ws'><child>text</child></root>";
		Source source = new StringSource(xml);
		Result result = new StringResult();

		helper.transform(source, result);

		assertThat(result.toString()).and(xml).ignoreWhitespace().areIdentical();
	}

}
