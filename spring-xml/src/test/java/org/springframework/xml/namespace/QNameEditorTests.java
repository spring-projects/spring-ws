/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
