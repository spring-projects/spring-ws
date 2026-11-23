/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.ws.transport.MockTransportInputStream;
import org.springframework.ws.transport.TransportInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link AxiomSoapMessageFactory}.
 *
 * @author Stephane Nicoll
 */
class AxiomSoapMessageFactoryTests {

	@Test
	void messageWithDoctypeDeclarationIsRejected(@TempDir Path tempDir) {
		assertThatExceptionOfType(AxiomSoapMessageCreationException.class)
			.isThrownBy(() -> createMessage(new AxiomSoapMessageFactory(), withExternalEntity(tempDir)))
			.withMessageContaining("DOCTYPE is not allowed");
	}

	@Test
	void messageWithDoctypeDeclarationIsRejectedEvenWhenExternalEntitiesAreEnabled(@TempDir Path tempDir) {
		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
		assertThatExceptionOfType(AxiomSoapMessageCreationException.class)
			.isThrownBy(() -> createMessage(messageFactory, withExternalEntity(tempDir)))
			.withMessageContaining("DOCTYPE is not allowed");
	}

	@Test
	void messageWithoutDoctypeDeclarationIsAccepted() throws Exception {
		AxiomSoapMessage message = createMessage(new AxiomSoapMessageFactory(), envelope("", "<echo>Hello</echo>"));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		message.writeTo(out);
		assertThat(out.toString(StandardCharsets.UTF_8)).contains("<echo>Hello</echo>");
	}

	private String withExternalEntity(Path directory) throws Exception {
		Path secret = Files.writeString(directory.resolve("secret.txt"), "s3cr3t");
		return envelope("<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"" + secret.toUri() + "\">]>", "<echo>&xxe;</echo>");
	}

	private String envelope(String doctype, String payload) {
		return doctype + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" + "<soap:Body>"
				+ payload + "</soap:Body></soap:Envelope>";
	}

	private AxiomSoapMessage createMessage(AxiomSoapMessageFactory messageFactory, String envelope) throws Exception {
		messageFactory.afterPropertiesSet();
		TransportInputStream in = new MockTransportInputStream(
				new ByteArrayInputStream(envelope.getBytes(StandardCharsets.UTF_8)));
		return messageFactory.createWebServiceMessage(in);
	}

}
