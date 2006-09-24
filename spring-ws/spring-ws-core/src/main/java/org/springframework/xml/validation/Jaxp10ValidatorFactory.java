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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import org.springframework.core.io.Resource;

/**
 * Internal class that uses JAXP 1.0 features to create <code>XmlValidator</code> instances.
 *
 * @author Arjen Poutsma
 */
abstract class Jaxp10ValidatorFactory {

    private static final String SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    private static final String SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

    static XmlValidator createValidator(Resource schemaResource, String schemaLanguage) {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(true);
        parserFactory.setValidating(true);
        return new Jaxp10Validator(parserFactory, schemaResource, schemaLanguage);
    }

    private static class Jaxp10Validator implements XmlValidator {

        private SAXParserFactory parserFactory;

        private TransformerFactory transformerFactory;

        private Resource schemaResource;

        private String schemaLanguage;

        private Jaxp10Validator(SAXParserFactory parserFactory, Resource schemaResource, String schemaLanguage) {
            this.parserFactory = parserFactory;
            this.schemaResource = schemaResource;
            this.schemaLanguage = schemaLanguage;
            transformerFactory = TransformerFactory.newInstance();
        }

        public SAXParseException[] validate(Source source) throws IOException {
            SAXParser parser = createSAXParser();
            ValidationErrorHandler errorHandler = new ValidationErrorHandler();
            try {
                if (source instanceof SAXSource) {
                    validateSAXSource((SAXSource) source, parser, errorHandler);
                }
                else if (source instanceof StreamSource) {
                    validateStreamSource((StreamSource) source, parser, errorHandler);
                }
                else if (source instanceof DOMSource) {
                    validateDOMSource((DOMSource) source, parser, errorHandler);
                }
                else {
                    throw new IllegalArgumentException("Source [" + source.getClass().getName() +
                            "] is neither SAXSource, DOMSource, nor StreamSource");
                }
                return errorHandler.getErrors();
            }
            catch (SAXException ex) {
                throw new XmlValidationException("Could not validate source: " + ex.getMessage(), ex);
            }
        }

        private void validateDOMSource(DOMSource domSource, SAXParser parser, ValidationErrorHandler errorHandler)
                throws IOException, SAXException {
            try {
                // Sadly, JAXP 1.0 DOM doesn't implement DOM level 3, so we cannot use Document.normalizeDocument()
                // Instead, we write the Document to a Stream, and validate that
                Transformer transformer = transformerFactory.newTransformer();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                transformer.transform(domSource, new StreamResult(outputStream));
                ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                validateStreamSource(new StreamSource(inputStream), parser, errorHandler);
            }
            catch (TransformerException ex) {
                throw new XmlValidationException("Could not validate DOM source: " + ex.getMessage(), ex);
            }

        }

        private void validateStreamSource(StreamSource streamSource,
                                          SAXParser parser,
                                          ValidationErrorHandler errorHandler) throws SAXException, IOException {
            if (streamSource.getInputStream() != null) {
                parser.parse(streamSource.getInputStream(), errorHandler);
            }
            else if (streamSource.getReader() != null) {
                parser.parse(new InputSource(streamSource.getReader()), errorHandler);
            }
            else {
                throw new IllegalArgumentException("StreamSource contains neither InputStream nor Reader");
            }
        }

        private void validateSAXSource(SAXSource source, SAXParser parser, ValidationErrorHandler errorHandler)
                throws SAXException, IOException {
            parser.parse(source.getInputSource(), errorHandler);
        }

        private SAXParser createSAXParser() throws IOException {
            try {
                SAXParser parser = parserFactory.newSAXParser();
                parser.setProperty(SCHEMA_LANGUAGE, schemaLanguage);
                parser.setProperty(SCHEMA_SOURCE, schemaResource.getFile());
                return parser;
            }
            catch (ParserConfigurationException ex) {
                throw new XmlValidationException("Could not create SAXParser: " + ex.getMessage(), ex);
            }
            catch (SAXException ex) {
                throw new XmlValidationException("Could not create SAXParser: " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * <code>DefaultHandler</code> extension that stores errors and fatal errors in a list.
     */
    private static class ValidationErrorHandler extends DefaultHandler {

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
