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

import javax.xml.namespace.QName;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class QNameEditorTest {

	private QNameEditor editor;

	@Before
	public void setUp() throws Exception {
		editor = new QNameEditor();
	}

	@Test
	public void testNamespaceLocalPartPrefix() throws Exception {
		QName qname = new QName("namespace", "localpart", "prefix");
		doTest(qname);
	}

	@Test
	public void testNamespaceLocalPart() throws Exception {
		QName qname = new QName("namespace", "localpart");
		doTest(qname);
	}

	@Test
	public void testLocalPart() throws Exception {
		QName qname = new QName("localpart");
		doTest(qname);
	}

	private void doTest(QName qname) {
		editor.setValue(qname);
		String text = editor.getAsText();
		Assert.assertNotNull("getAsText returns null", text);
		editor.setAsText(text);
		QName result = (QName) editor.getValue();
		Assert.assertNotNull("getValue returns null", result);
		Assert.assertEquals("Parsed QName local part is not equal to original", qname.getLocalPart(),
				result.getLocalPart());
		Assert.assertEquals("Parsed QName prefix is not equal to original", qname.getPrefix(), result.getPrefix());
		Assert.assertEquals("Parsed QName namespace is not equal to original", qname.getNamespaceURI(),
				result.getNamespaceURI());
	}
}
