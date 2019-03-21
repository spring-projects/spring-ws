/*
 * Copyright 2018 the original author or authors.
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
package org.springframework.xml.xsd;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Greg Turnquist
 * @see 3.0.5
 */
public class DocumentBuilderFactoryUtils {

	private static final Log log = LogFactory.getLog(DocumentBuilderFactoryUtils.class);

	/**
	 * Build a {@link DocumentBuilderFactory} then set properties to prevent external entity access.
	 *
	 * @see DocumentBuilderFactory#newInstance()
	 */
	public static DocumentBuilderFactory newInstance() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		try {
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		} catch (IllegalArgumentException e) {
			if (log.isWarnEnabled()) {
				log.warn(XMLConstants.ACCESS_EXTERNAL_DTD + " property not supported by " + factory.getClass().getCanonicalName());
			}
		}

		try {
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		} catch (IllegalArgumentException e) {
			if (log.isWarnEnabled()) {
				log.warn(XMLConstants.ACCESS_EXTERNAL_SCHEMA + " property not supported by " + factory.getClass().getCanonicalName());
			}
		}

		try {
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		} catch (ParserConfigurationException e) {
			if (log.isWarnEnabled()) {
				log.warn("FEATURE 'http://apache.org/xml/features/disallow-doctype-decl' is probably not supported by " + factory.getClass().getCanonicalName());
			}
		}

		try {
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		} catch (ParserConfigurationException e) {
			if (log.isWarnEnabled()) {
				log.warn("FEATURE 'http://xml.org/sax/features/external-general-entities' is probably not supported by " + factory.getClass().getCanonicalName());
			}
		}

		try {
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		} catch (ParserConfigurationException e) {
			if (log.isWarnEnabled()) {
				log.warn("FEATURE 'http://xml.org/sax/features/external-parameter-entities' is probably not supported by " + factory.getClass().getCanonicalName());
			}
		}

		try {
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (ParserConfigurationException e) {
			if (log.isWarnEnabled()) {
				log.warn("FEATURE 'http://apache.org/xml/features/nonvalidating/load-external-dtd' is probably not supported by " + factory.getClass().getCanonicalName());
			}
		}

		try {
			factory.setXIncludeAware(false);
			factory.setExpandEntityReferences(false);
		} catch (Exception e) {
			if (log.isWarnEnabled()) {
				log.warn("Caught " + e.getMessage() + " attempting to configure your XML parser.");
			}
		}

		return factory;
	}
}
