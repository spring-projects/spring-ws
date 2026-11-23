/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.validation;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Subinterface of {@link ErrorHandler} that allows the registered errors to be retrieved.
 *
 * @author Arjen Poutsma
 * @since 2.0.1
 */
public interface ValidationErrorHandler extends ErrorHandler {

	/**
	 * Returns the errors collected by this error handler.
	 * @return the errors
	 */
	SAXParseException[] getErrors();

}
