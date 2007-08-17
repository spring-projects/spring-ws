/*
 * Copyright 2006 the original author or authors.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;

import org.springframework.core.io.Resource;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Internal class that uses JAXP 1.0 features to create <code>XmlValidator</code> instances.
 *
 * @author Arjen Poutsma
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

    private static class Jaxp13Validator implements XmlValidator {

        private Schema schema;

        public Jaxp13Validator(Schema schema) {
            this.schema = schema;
        }

        public SAXParseException[] validate(Source source) throws IOException {
            javax.xml.validation.Validator validator = schema.newValidator();
            ValidationErrorHandler errorHandler = new ValidationErrorHandler();
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

    /** <code>ErrorHandler</code> implementation that stores errors and fatal errors in a list. */
    private static class ValidationErrorHandler implements ErrorHandler {

        private List errors = new ArrayList();

        private SAXParseException[] getErrors() {
            return (SAXParseException[]) errors.toArray(new SAXParseException[errors.size()]);
        }

        public void warning(SAXParseException ex) throws SAXException {
        }

        public void error(SAXParseException ex) throws SAXException {
            errors.add(ex);
        }

        public void fatalError(SAXParseException ex) throws SAXException {
            errors.add(ex);
        }
    }
}
