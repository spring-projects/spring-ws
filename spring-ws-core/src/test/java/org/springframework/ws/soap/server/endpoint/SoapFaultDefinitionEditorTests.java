/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
