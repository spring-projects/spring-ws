/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.sax;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link SaxUtils}.
 *
 * @author Stephane Nicoll
 */
class SaxUtilsTests {

	@Test
	void namespaceAwareXmlReaderRejectsDoctypeDeclaration(@TempDir Path tempDir) throws Exception {
		Path secret = Files.writeString(tempDir.resolve("secret.txt"), "s3cr3t");
		String xml = "<!DOCTYPE root [<!ENTITY xxe SYSTEM \"" + secret.toUri() + "\">]><root>&xxe;</root>";
		XMLReader xmlReader = SaxUtils.namespaceAwareXmlReader();
		assertThatExceptionOfType(SAXParseException.class)
			.isThrownBy(() -> xmlReader.parse(new InputSource(new StringReader(xml))))
			.withMessageContaining("DOCTYPE");
	}

	@Test
	void namespaceAwareXmlReaderParsesDocumentWithoutDoctypeDeclaration() throws Exception {
		XMLReader xmlReader = SaxUtils.namespaceAwareXmlReader();
		StringBuilder text = new StringBuilder();
		xmlReader.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
			@Override
			public void characters(char[] ch, int start, int length) {
				text.append(ch, start, length);
			}
		});
		xmlReader.parse(new InputSource(new StringReader("<root>Hello</root>")));
		assertThat(text.toString()).isEqualTo("Hello");
	}

	@Test
	void testGetSystemId() {

		Resource resource = new FileSystemResource("/path with spaces/file with spaces.txt");
		String systemId = SaxUtils.getSystemId(resource);

		assertThat(systemId).isNotNull();
		assertThat(systemId).endsWith("path%20with%20spaces/file%20with%20spaces.txt");
	}

}
