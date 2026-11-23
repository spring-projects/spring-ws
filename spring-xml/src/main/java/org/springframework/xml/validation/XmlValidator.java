/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.validation;

import java.io.IOException;

import javax.xml.transform.Source;

import org.jspecify.annotations.Nullable;
import org.xml.sax.SAXParseException;

/**
 * Simple processor that validates a given {@link Source}. Can be created via the
 * {@link XmlValidatorFactory}.
 * <p>
 * Instances of this class are designed to be thread safe.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see XmlValidatorFactory#createValidator(org.springframework.core.io.Resource, String)
 */
public interface XmlValidator {

	/**
	 * Validates the given {@link Source}, and returns an array of
	 * {@link SAXParseException}s as result. The array will be empty if no validation
	 * errors are found.
	 * @param source the input document
	 * @return an array of {@code SAXParseException}s
	 * @throws IOException if the {@code source} cannot be read
	 * @throws XmlValidationException if the {@code source} cannot be validated
	 */
	SAXParseException[] validate(Source source) throws IOException;

	/**
	 * Validates the given {@link Source} and {@link ValidationErrorHandler}, and returns
	 * an array of {@link SAXParseException}s as result. The array will be empty if no
	 * validation errors are found.
	 * @param source the input document
	 * @param errorHandler the error handler to use. May be {@code null}, in which case a
	 * default will be used.
	 * @return an array of {@code SAXParseException}s
	 * @throws IOException if the {@code source} cannot be read
	 * @throws XmlValidationException if the {@code source} cannot be validated
	 */
	SAXParseException[] validate(Source source, @Nullable ValidationErrorHandler errorHandler) throws IOException;

}
