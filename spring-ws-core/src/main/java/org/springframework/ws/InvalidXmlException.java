/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws;

/**
 * Exception thrown when a {@link WebServiceMessageFactory} cannot parse the XML passed on
 * to {@link WebServiceMessageFactory#createWebServiceMessage(java.io.InputStream)}.
 *
 * @author Arjen Poutsma
 * @since 2.0.4
 */
@SuppressWarnings("serial")
public final class InvalidXmlException extends WebServiceException {

	public InvalidXmlException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
