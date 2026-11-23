/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static org.assertj.core.api.Assertions.assertThatException;

/**
 * Tests for {@link DocumentBuilderFactoryUtils}.
 *
 * @author Stephane Nicoll
 */
class DocumentBuilderFactoryUtilsTest {

	@Test
	void parseRejectsDoctypeWithExternalEntity() throws Exception {
		String xxe = "<?xml version=\"1.0\"?><!DOCTYPE r [<!ENTITY e SYSTEM \"file:///etc/passwd\">]><r>&e;</r>";
		DocumentBuilderFactory factory = DocumentBuilderFactoryUtils.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		assertThatException().isThrownBy(() -> builder.parse(new InputSource(new StringReader(xxe))))
			.isInstanceOf(SAXException.class);
	}

}
