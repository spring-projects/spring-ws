/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.http;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;

import org.springframework.util.StringUtils;
import org.springframework.xml.transform.TraxUtils;

/**
 * Utility class that determines the last modified date of a given {@link Source}.
 *
 * @author Arjen Poutsma
 * @since 1.5.3
 */
final class LastModifiedHelper {

	private LastModifiedHelper() {
	}

	/**
	 * Returns the last modified date of the given {@link Source}.
	 * @param source the source
	 * @return the last modified date, as a long
	 */
	static long getLastModified(Source source) {
		if (source instanceof DOMSource) {
			Document document = TraxUtils.getDocument((DOMSource) source);
			return (document != null) ? getLastModified(document.getDocumentURI()) : -1;
		}
		else {
			return getLastModified(source.getSystemId());
		}
	}

	private static long getLastModified(String systemId) {
		if (StringUtils.hasText(systemId)) {
			try {
				URI systemIdUri = new URI(systemId);
				if ("file".equals(systemIdUri.getScheme())) {
					File documentFile = new File(systemIdUri);
					if (documentFile.exists()) {
						return documentFile.lastModified();
					}
				}
			}
			catch (URISyntaxException ex) {
				// ignore
			}
		}
		return -1;
	}

}
