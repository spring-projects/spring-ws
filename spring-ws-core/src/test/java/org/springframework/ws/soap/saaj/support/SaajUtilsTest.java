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

package org.springframework.ws.soap.saaj.support;

import static org.assertj.core.api.Assertions.*;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.Name;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;

import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.w3c.dom.Document;
import org.xmlunit.assertj.XmlAssert;

public class SaajUtilsTest {

	private MessageFactory messageFactory;

	@BeforeEach
	public void setUp() throws Exception {
		messageFactory = MessageFactory.newInstance();
	}

	@Test
	public void testToName() throws Exception {

		SOAPMessage message = messageFactory.createMessage();
		QName qName = new QName("localPart");
		SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
		Name name = SaajUtils.toName(qName, envelope);

		assertThat(name).isNotNull();
		assertThat(name.getLocalName()).isEqualTo(qName.getLocalPart());
		assertThat(StringUtils.hasLength(name.getPrefix())).isFalse();
		assertThat(StringUtils.hasLength(name.getURI())).isFalse();
	}

	@Test
	public void testToNameNamespace() throws Exception {

		SOAPMessage message = messageFactory.createMessage();
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
	public void testToNameNamespacePrefix() throws Exception {

		SOAPMessage message = messageFactory.createMessage();
		QName qName = new QName("namespace", "localPart", "prefix");
		SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
		Name name = SaajUtils.toName(qName, envelope);

		assertThat(name).isNotNull();
		assertThat(name.getURI()).isEqualTo(qName.getNamespaceURI());
		assertThat(name.getLocalName()).isEqualTo(qName.getLocalPart());
		assertThat(name.getPrefix()).isEqualTo(qName.getPrefix());
	}

	@Test
	public void testToQName() throws Exception {

		SOAPMessage message = messageFactory.createMessage();
		Name name = message.getSOAPPart().getEnvelope().createName("localPart", null, null);
		QName qName = SaajUtils.toQName(name);

		assertThat(qName).isNotNull();
		assertThat(qName.getNamespaceURI()).isEqualTo(name.getURI());
		assertThat(StringUtils.hasLength(qName.getPrefix())).isFalse();
		assertThat(StringUtils.hasLength(qName.getNamespaceURI())).isFalse();
	}

	@Test
	public void testToQNameNamespace() throws Exception {

		SOAPMessage message = messageFactory.createMessage();
		Name name = message.getSOAPPart().getEnvelope().createName("localPart", null, "namespace");
		QName qName = SaajUtils.toQName(name);

		assertThat(qName).isNotNull();
		assertThat(qName.getNamespaceURI()).isEqualTo(name.getURI());
		assertThat(qName.getLocalPart()).isEqualTo(name.getLocalName());
		assertThat(StringUtils.hasLength(qName.getPrefix())).isFalse();
	}

	@Test
	public void testToQNamePrefixNamespace() throws Exception {

		SOAPMessage message = messageFactory.createMessage();
		Name name = message.getSOAPPart().getEnvelope().createName("localPart", "prefix", "namespace");
		QName qName = SaajUtils.toQName(name);

		assertThat(qName).isNotNull();
		assertThat(qName.getNamespaceURI()).isEqualTo(name.getURI());
		assertThat(qName.getLocalPart()).isEqualTo(name.getLocalName());
		assertThat(qName.getPrefix()).isEqualTo(name.getPrefix());
	}

	@Test
	public void testLoadMessage() throws Exception {

		DocumentBuilderFactory factory = DocumentBuilderFactoryUtils.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(getClass().getResourceAsStream("soapMessage.xml"));
		SOAPMessage soapMessage = SaajUtils.loadMessage(new ClassPathResource("soapMessage.xml", getClass()),
				messageFactory);

		XmlAssert.assertThat(document).and(soapMessage.getSOAPPart()).ignoreWhitespace().areIdentical();
	}

	@Test
	public void testGetSaajVersion() {
		assertThat(SaajUtils.getSaajVersion()).isEqualTo(SaajUtils.SAAJ_13);
	}

	@Test
	public void testGetSaajVersionInvalidEnvelope() {

		assertThatExceptionOfType(SOAPException.class).isThrownBy(() -> {

			Resource resource = new ClassPathResource("invalidNamespaceReferenceSoapMessage.xml", getClass());
			InputStream in = resource.getInputStream();
			MimeHeaders headers = new MimeHeaders();
			SOAPMessage soapMessage = messageFactory.createMessage(headers, in);
			SaajUtils.getSaajVersion(soapMessage);
		});
	}

}
