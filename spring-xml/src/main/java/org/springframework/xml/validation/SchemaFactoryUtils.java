/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.validation;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import org.springframework.util.ResourceUtils;

/**
 * General utilities for {@link SchemaFactory}.
 *
 * @author Greg Turnquist
 * @since 3.0.5
 */
public abstract class SchemaFactoryUtils {

	private static final Log log = LogFactory.getLog(SchemaFactoryUtils.class);

	/**
	 * Build a {@link SchemaFactory} and set properties to prevent external entities from
	 * accessing.
	 * @see SchemaFactory#newInstance(String)
	 */
	public static SchemaFactory newInstance(String schemaLanguage) {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(schemaLanguage);

		try {
			schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		}
		catch (SAXNotRecognizedException | SAXNotSupportedException ex) {
			if (log.isWarnEnabled()) {
				log.warn(XMLConstants.ACCESS_EXTERNAL_DTD + " property not supported by "
						+ schemaFactory.getClass().getCanonicalName());
			}

		}

		try {
			schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ResourceUtils.URL_PROTOCOL_FILE + ","
					+ "jar:file" + "," + "nested" + "," + ResourceUtils.URL_PROTOCOL_WSJAR);
		}
		catch (SAXNotRecognizedException | SAXNotSupportedException ex) {
			if (log.isWarnEnabled()) {
				log.warn(XMLConstants.ACCESS_EXTERNAL_SCHEMA + " property not supported by "
						+ schemaFactory.getClass().getCanonicalName());
			}
		}

		return schemaFactory;
	}

}
