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

package org.springframework.xml.validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;

import org.springframework.core.io.Resource;

/**
 * Internal class that uses JAXP 1.5 features to create an {@code XmlValidator} with
 * settings to prevent external entity access.
 *
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 3.0.5
 */
abstract class Jaxp15ValidatorFactory {

	private static final Log log = LogFactory.getLog(Jaxp15ValidatorFactory.class);

	static XmlValidator createValidator(Resource[] resources, String schemaLanguage) throws IOException {
		try {
			Schema schema = SchemaLoaderUtils.loadSchema(resources, schemaLanguage);
			return new Jaxp15Validator(schema);
		}
		catch (SAXException ex) {
			throw new XmlValidationException("Could not create Schema: " + ex.getMessage(), ex);
		}
	}

	private static final class Jaxp15Validator implements XmlValidator {

		private final Schema schema;

		Jaxp15Validator(Schema schema) {
			this.schema = schema;
		}

		@Override
		public SAXParseException[] validate(Source source) throws IOException {
			return validate(source, null);
		}

		@Override
		public SAXParseException[] validate(Source source, @Nullable ValidationErrorHandler errorHandler)
				throws IOException {
			if (errorHandler == null) {
				errorHandler = new DefaultValidationErrorHandler();
			}
			Validator validator = this.schema.newValidator();

			try {
				validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			}
			catch (SAXNotRecognizedException | SAXNotSupportedException ex) {
				if (log.isWarnEnabled()) {
					log.warn(XMLConstants.ACCESS_EXTERNAL_DTD + " property not supported by "
							+ validator.getClass().getCanonicalName());
				}
			}

			try {
				validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			}
			catch (SAXNotRecognizedException | SAXNotSupportedException ex) {
				if (log.isWarnEnabled()) {
					log.warn(XMLConstants.ACCESS_EXTERNAL_SCHEMA + " property not supported by "
							+ validator.getClass().getCanonicalName());
				}
			}

			validator.setErrorHandler(errorHandler);
			try {
				validator.validate(source);
				return errorHandler.getErrors();
			}
			catch (SAXException ex) {
				throw new XmlValidationException("Could not validate source: " + ex.getMessage(), ex);
			}
		}

	}

	/**
	 * {@code ErrorHandler} implementation that stores errors and fatal errors in a list.
	 */
	private static final class DefaultValidationErrorHandler implements ValidationErrorHandler {

		private final List<SAXParseException> errors = new ArrayList<>();

		@Override
		public SAXParseException[] getErrors() {
			return this.errors.toArray(new SAXParseException[0]);
		}

		@Override
		public void warning(SAXParseException ex) throws SAXException {
		}

		@Override
		public void error(SAXParseException ex) throws SAXException {
			this.errors.add(ex);
		}

		@Override
		public void fatalError(SAXParseException ex) throws SAXException {
			this.errors.add(ex);
		}

	}

}
