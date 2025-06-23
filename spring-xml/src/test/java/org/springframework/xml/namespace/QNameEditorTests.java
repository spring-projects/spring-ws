/*
 * Copyright 2005-present the original author or authors.
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

package org.springframework.xml.namespace;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QNameEditorTests {

	private QNameEditor editor;

	@BeforeEach
	void setUp() {

		this.editor = new QNameEditor();
	}

	@Test
	void testNamespaceLocalPartPrefix() {

		QName qname = new QName("namespace", "localpart", "prefix");
		doTest(qname);
	}

	@Test
	void testNamespaceLocalPart() {

		QName qname = new QName("namespace", "localpart");
		doTest(qname);
	}

	@Test
	void testLocalPart() {

		QName qname = new QName("localpart");
		doTest(qname);
	}

	private void doTest(QName qname) {

		this.editor.setValue(qname);

		String text = this.editor.getAsText();

		assertThat(text).isNotNull();

		this.editor.setAsText(text);
		QName result = (QName) this.editor.getValue();

		assertThat(result).isNotNull();
		assertThat(result.getLocalPart()).isEqualTo(qname.getLocalPart());
		assertThat(result.getPrefix()).isEqualTo(qname.getPrefix());
		assertThat(result.getNamespaceURI()).isEqualTo(qname.getNamespaceURI());
	}

}
