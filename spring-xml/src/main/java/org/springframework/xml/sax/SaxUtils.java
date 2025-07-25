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

package org.springframework.xml.sax;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.springframework.core.io.Resource;

/**
 * Convenient utility methods for dealing with SAX.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class SaxUtils {

	private static final Log logger = LogFactory.getLog(SaxUtils.class);

	/**
	 * Create a default {@link XMLReader} that is
	 * {@linkplain SAXParserFactory#setNamespaceAware(boolean) namespace aware}.
	 * @return a new {@link XMLReader}
	 */
	public static XMLReader namespaceAwareXmlReader() throws SAXException {
		try {
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			parserFactory.setNamespaceAware(true);
			return parserFactory.newSAXParser().getXMLReader();
		}
		catch (ParserConfigurationException ex) {
			throw new IllegalStateException(ex);
		}
	}

	/**
	 * Creates a SAX {@code InputSource} from the given resource. Sets the system
	 * identifier to the resource's {@code URL}, if available.
	 * @param resource the resource
	 * @return the input source created from the resource
	 * @throws IOException if an I/O exception occurs
	 * @see InputSource#setSystemId(String)
	 * @see #getSystemId(org.springframework.core.io.Resource)
	 */
	public static InputSource createInputSource(Resource resource) throws IOException {
		InputSource inputSource = new InputSource(resource.getInputStream());
		inputSource.setSystemId(getSystemId(resource));
		return inputSource;
	}

	/**
	 * Retrieves the URL from the given resource as System ID. Returns {@code null} if it
	 * cannot be opened.
	 */
	public static @Nullable String getSystemId(Resource resource) {
		try {
			return new URI(resource.getURL().toExternalForm()).toString();
		}
		catch (IOException | URISyntaxException ex) {
			logger.debug("Could not get System ID from [" + resource + "], ex");
			return null;
		}
	}

}
