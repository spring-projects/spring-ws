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

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ResourceUtils;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * @author Greg Turnquist
 * @since 3.0.5
 */
public class SchemaFactoryUtils {

	private static final Log log = LogFactory.getLog(SchemaFactoryUtils.class);

	/**
	 * Build a {@link SchemaFactory} and set properties to prevent external entities from accessing.
	 *
	 * @see SchemaFactory#newInstance(String) 
	 */
	public static SchemaFactory newInstance(String schemaLanguage) {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(schemaLanguage);

		try {
			schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		} catch (SAXNotRecognizedException | SAXNotSupportedException e) {
			if (log.isWarnEnabled()) {
				log.warn(XMLConstants.ACCESS_EXTERNAL_DTD + " property not supported by " + schemaFactory.getClass().getCanonicalName());
			}

		}

		try {
			schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ResourceUtils.URL_PROTOCOL_FILE + "," + "jar:file" + "," + ResourceUtils.URL_PROTOCOL_WSJAR);
		} catch (SAXNotRecognizedException | SAXNotSupportedException e) {
			if (log.isWarnEnabled()) {
				log.warn(XMLConstants.ACCESS_EXTERNAL_SCHEMA + " property not supported by " + schemaFactory.getClass().getCanonicalName());
			}
		}

		return schemaFactory;
	}
}
