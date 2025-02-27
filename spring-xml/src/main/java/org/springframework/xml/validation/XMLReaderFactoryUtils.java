/*
 * Copyright 2005-2025 the original author or authors.
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
package org.springframework.xml.validation;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author Greg Turnquist
 * @since 3.0.5
 */
public class XMLReaderFactoryUtils {

	/**
	 * Build a {@link XMLReader} and set properties to prevent external entity access.
	 * @see SAXParser#getXMLReader()
	 */
	public static XMLReader createXMLReader() throws SAXException {
		XMLReader xmlReader = namespaceAwareXmlReader();
		xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
		xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		return xmlReader;
	}

	private static XMLReader namespaceAwareXmlReader() throws SAXException {
		try {
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			parserFactory.setNamespaceAware(true);
			return parserFactory.newSAXParser().getXMLReader();
		}
		catch (ParserConfigurationException ex) {
			throw new IllegalStateException(ex);
		}

	}

}
