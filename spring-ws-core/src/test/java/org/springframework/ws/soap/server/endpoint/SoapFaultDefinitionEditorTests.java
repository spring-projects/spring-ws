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

package org.springframework.ws.soap.server.endpoint;

import java.util.Locale;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SoapFaultDefinitionEditorTests {

	private SoapFaultDefinitionEditor editor;

	@BeforeEach
	void setUp() {
		this.editor = new SoapFaultDefinitionEditor();
	}

	@Test
	void testSetAsTextNoLocale() {

		this.editor.setAsText("Server, Server error");
		SoapFaultDefinition definition = (SoapFaultDefinition) this.editor.getValue();

		assertThat(definition).isNotNull();
		assertThat(definition.getFaultCode()).isEqualTo(new QName("Server"));
		assertThat(definition.getFaultStringOrReason()).isEqualTo("Server error");
		assertThat(definition.getLocale()).isEqualTo(Locale.ENGLISH);
	}

	@Test
	void testSetAsTextLocale() {

		this.editor.setAsText("Server, Server error, nl");
		SoapFaultDefinition definition = (SoapFaultDefinition) this.editor.getValue();

		assertThat(definition).isNotNull();
		assertThat(definition.getFaultCode()).isEqualTo(new QName("Server"));
		assertThat(definition.getFaultStringOrReason()).isEqualTo("Server error");
		assertThat(definition.getLocale()).isEqualTo(new Locale("nl"));
	}

	@Test
	void testSetAsTextSender() {

		this.editor.setAsText("SENDER, Server error");
		SoapFaultDefinition definition = (SoapFaultDefinition) this.editor.getValue();

		assertThat(definition).isNotNull();
		assertThat(definition.getFaultCode()).isEqualTo(SoapFaultDefinition.SENDER);
		assertThat(definition.getFaultStringOrReason()).isEqualTo("Server error");
	}

	@Test
	void testSetAsTextReceiver() {

		this.editor.setAsText("RECEIVER, Server error");
		SoapFaultDefinition definition = (SoapFaultDefinition) this.editor.getValue();

		assertThat(definition).isNotNull();
		assertThat(definition.getFaultCode()).isEqualTo(SoapFaultDefinition.RECEIVER);
		assertThat(definition.getFaultStringOrReason()).isEqualTo("Server error");
	}

	@Test
	void testSetAsTextIllegalArgument() {
		this.editor.setAsText("SOAP-ENV:Server");
	}

	@Test
	void testSetAsTextEmpty() {

		this.editor.setAsText("");

		assertThat(this.editor.getValue()).isNull();
	}

}
