/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj.support;

import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.Name;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.xml.DocumentBuilderFactoryUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SaajUtilsTests {

	private MessageFactory messageFactory;

	@BeforeEach
	void setUp() throws Exception {
		this.messageFactory = MessageFactory.newInstance();
	}

	@Test
	void testToName() throws Exception {

		SOAPMessage message = this.messageFactory.createMessage();
		QName qName = new QName("localPart");
		SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
		Name name = SaajUtils.toName(qName, envelope);

		assertThat(name).isNotNull();
		assertThat(name.getLocalName()).isEqualTo(qName.getLocalPart());
		assertThat(StringUtils.hasLength(name.getPrefix())).isFalse();
		assertThat(StringUtils.hasLength(name.getURI())).isFalse();
	}

	@Test
	void testToNameNamespace() throws Exception {

		SOAPMessage message = this.messageFactory.createMessage();
		QName qName = new QName("namespace", "localPart");
		SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
		envelope.addNamespaceDeclaration("prefix", "namespace");
		Name name = SaajUtils.toName(qName, envelope);

		assertThat(name).isNotNull();
		assertThat(name.getURI()).isEqualTo(qName.getNamespaceURI());
		assertThat(name.getLocalName()).isEqualTo(qName.getLocalPart());
		assertThat(name.getPrefix()).isEqualTo("prefix");
	}

	@Test
	void testToNameNamespacePrefix() throws Exception {

		SOAPMessage message = this.messageFactory.createMessage();
		QName qName = new QName("namespace", "localPart", "prefix");
		SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
		Name name = SaajUtils.toName(qName, envelope);

		assertThat(name).isNotNull();
		assertThat(name.getURI()).isEqualTo(qName.getNamespaceURI());
		assertThat(name.getLocalName()).isEqualTo(qName.getLocalPart());
		assertThat(name.getPrefix()).isEqualTo(qName.getPrefix());
	}

	@Test
	void testToQName() throws Exception {

		SOAPMessage message = this.messageFactory.createMessage();
		Name name = message.getSOAPPart().getEnvelope().createName("localPart", null, null);
		QName qName = SaajUtils.toQName(name);

		assertThat(qName).isNotNull();
		assertThat(qName.getNamespaceURI()).isEqualTo(name.getURI());
		assertThat(StringUtils.hasLength(qName.getPrefix())).isFalse();
		assertThat(StringUtils.hasLength(qName.getNamespaceURI())).isFalse();
	}

	@Test
	void testToQNameNamespace() throws Exception {

		SOAPMessage message = this.messageFactory.createMessage();
		Name name = message.getSOAPPart().getEnvelope().createName("localPart", null, "namespace");
		QName qName = SaajUtils.toQName(name);

		assertThat(qName).isNotNull();
		assertThat(qName.getNamespaceURI()).isEqualTo(name.getURI());
		assertThat(qName.getLocalPart()).isEqualTo(name.getLocalName());
		assertThat(StringUtils.hasLength(qName.getPrefix())).isFalse();
	}

	@Test
	void testToQNamePrefixNamespace() throws Exception {

		SOAPMessage message = this.messageFactory.createMessage();
		Name name = message.getSOAPPart().getEnvelope().createName("localPart", "prefix", "namespace");
		QName qName = SaajUtils.toQName(name);

		assertThat(qName).isNotNull();
		assertThat(qName.getNamespaceURI()).isEqualTo(name.getURI());
		assertThat(qName.getLocalPart()).isEqualTo(name.getLocalName());
		assertThat(qName.getPrefix()).isEqualTo(name.getPrefix());
	}

	@Test
	void testLoadMessage() throws Exception {

		DocumentBuilderFactory factory = DocumentBuilderFactoryUtils.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(getClass().getResourceAsStream("soapMessage.xml"));
		SOAPMessage soapMessage = SaajUtils.loadMessage(new ClassPathResource("soapMessage.xml", getClass()),
				this.messageFactory);

		XmlAssert.assertThat(document).and(soapMessage.getSOAPPart()).ignoreWhitespace().areIdentical();
	}

	@Test
	void testGetSaajVersion() {
		assertThat(SaajUtils.getSaajVersion()).isEqualTo(SaajUtils.SAAJ_13);
	}

	@Test
	void testGetSaajVersionInvalidEnvelope() {

		assertThatExceptionOfType(SOAPException.class).isThrownBy(() -> {

			Resource resource = new ClassPathResource("invalidNamespaceReferenceSoapMessage.xml", getClass());
			InputStream in = resource.getInputStream();
			MimeHeaders headers = new MimeHeaders();
			SOAPMessage soapMessage = this.messageFactory.createMessage(headers, in);
			SaajUtils.getSaajVersion(soapMessage);
		});
	}

}
