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

package org.springframework.xml.namespace;

import static org.assertj.core.api.Assertions.*;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class QNameEditorTest {

	private QNameEditor editor;

	@BeforeEach
	public void setUp() throws Exception {

		editor = new QNameEditor();
	}

	@Test
	public void testNamespaceLocalPartPrefix() {

		QName qname = new QName("namespace", "localpart", "prefix");
		doTest(qname);
	}

	@Test
	public void testNamespaceLocalPart() {

		QName qname = new QName("namespace", "localpart");
		doTest(qname);
	}

	@Test
	public void testLocalPart() {

		QName qname = new QName("localpart");
		doTest(qname);
	}

	private void doTest(QName qname) {

		editor.setValue(qname);

		String text = editor.getAsText();

		assertThat(text).isNotNull();

		editor.setAsText(text);
		QName result = (QName) editor.getValue();

		assertThat(result).isNotNull();
		assertThat(result.getLocalPart()).isEqualTo(qname.getLocalPart());
		assertThat(result.getPrefix()).isEqualTo(qname.getPrefix());
		assertThat(result.getNamespaceURI()).isEqualTo(qname.getNamespaceURI());
	}
}
