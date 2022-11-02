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

package org.springframework.ws.soap.server.endpoint;

import static org.assertj.core.api.Assertions.*;

import java.util.Locale;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SoapFaultDefinitionEditorTest {

	private SoapFaultDefinitionEditor editor;

	@BeforeEach
	public void setUp() throws Exception {
		editor = new SoapFaultDefinitionEditor();
	}

	@Test
	public void testSetAsTextNoLocale() {

		editor.setAsText("Server, Server error");
		SoapFaultDefinition definition = (SoapFaultDefinition) editor.getValue();

		assertThat(definition).isNotNull();
		assertThat(definition.getFaultCode()).isEqualTo(new QName("Server"));
		assertThat(definition.getFaultStringOrReason()).isEqualTo("Server error");
		assertThat(definition.getLocale()).isEqualTo(Locale.ENGLISH);
	}

	@Test
	public void testSetAsTextLocale() {

		editor.setAsText("Server, Server error, nl");
		SoapFaultDefinition definition = (SoapFaultDefinition) editor.getValue();

		assertThat(definition).isNotNull();
		assertThat(definition.getFaultCode()).isEqualTo(new QName("Server"));
		assertThat(definition.getFaultStringOrReason()).isEqualTo("Server error");
		assertThat(definition.getLocale()).isEqualTo(new Locale("nl"));
	}

	@Test
	public void testSetAsTextSender() {

		editor.setAsText("SENDER, Server error");
		SoapFaultDefinition definition = (SoapFaultDefinition) editor.getValue();

		assertThat(definition).isNotNull();
		assertThat(definition.getFaultCode()).isEqualTo(SoapFaultDefinition.SENDER);
		assertThat(definition.getFaultStringOrReason()).isEqualTo("Server error");
	}

	@Test
	public void testSetAsTextReceiver() {

		editor.setAsText("RECEIVER, Server error");
		SoapFaultDefinition definition = (SoapFaultDefinition) editor.getValue();

		assertThat(definition).isNotNull();
		assertThat(definition.getFaultCode()).isEqualTo(SoapFaultDefinition.RECEIVER);
		assertThat(definition.getFaultStringOrReason()).isEqualTo("Server error");
	}

	@Test
	public void testSetAsTextIllegalArgument() {
		editor.setAsText("SOAP-ENV:Server");
	}

	@Test
	public void testSetAsTextEmpty() {

		editor.setAsText("");

		assertThat(editor.getValue()).isNull();
	}
}
