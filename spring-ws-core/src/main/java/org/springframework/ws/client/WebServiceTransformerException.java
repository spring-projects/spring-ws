/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client;

import javax.xml.transform.TransformerException;

/**
 * Exception thrown whenever a transformation error occurs <i>on the client-side</i>.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class WebServiceTransformerException extends WebServiceClientException {

	/**
	 * Create a new instance of the {@code WebServiceTransformerException} class.
	 * @param msg the detail message
	 */
	public WebServiceTransformerException(String msg) {
		super(msg);
	}

	/**
	 * Create a new instance of the {@code WebServiceTransformerException} class.
	 * @param msg the detail message
	 * @param ex the root {@link Throwable exception}
	 */
	public WebServiceTransformerException(String msg, TransformerException ex) {
		super(msg, ex);
	}

}
