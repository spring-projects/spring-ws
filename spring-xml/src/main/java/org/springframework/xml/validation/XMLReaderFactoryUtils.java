/*
 * Copyright 2018 the original author or authors.
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
package org.springframework.xml.validation;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Greg Turnquist
 * @since 3.0.5
 */
public class XMLReaderFactoryUtils {

	/**
	 * Build a {@link XMLReader} and set properties to prevent external entity access.
	 *
	 * @see XMLReaderFactory#createXMLReader() 
	 */
	public static XMLReader createXMLReader() throws SAXException {
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();

		xmlReader.setFeature("https://apache.org/xml/features/disallow-doctype-decl", true);
		xmlReader.setFeature("https://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		xmlReader.setFeature("http://www.xml.org/sax/features/external-general-entities", false);
		xmlReader.setFeature("http://www.xml.org/sax/features/external-parameter-entities", false);

		return xmlReader;
	}
}
