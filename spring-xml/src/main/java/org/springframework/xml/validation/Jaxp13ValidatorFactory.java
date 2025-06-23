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

import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.jspecify.annotations.Nullable;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.springframework.core.io.Resource;

/**
 * Internal class that uses JAXP 1.0 features to create {@code XmlValidator} instances.
 *
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 1.0.0
 */
abstract class Jaxp13ValidatorFactory {

	static XmlValidator createValidator(Resource[] resources, String schemaLanguage) throws IOException {
		try {
			Schema schema = SchemaLoaderUtils.loadSchema(resources, schemaLanguage);
			return new Jaxp13Validator(schema);
		}
		catch (SAXException ex) {
			throw new XmlValidationException("Could not create Schema: " + ex.getMessage(), ex);
		}
	}

	private static final class Jaxp13Validator implements XmlValidator {

		private final Schema schema;

		Jaxp13Validator(Schema schema) {
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
