/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.mime;

import org.springframework.ws.WebServiceMessageException;

/**
 * Exception thrown when a MIME attachment could not be accessed.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see Attachment
 */
@SuppressWarnings("serial")
public class AttachmentException extends WebServiceMessageException {

	public AttachmentException(String msg) {
		super(msg);
	}

	public AttachmentException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public AttachmentException(Throwable ex) {
		super("Could not access body: " + ex.getMessage(), ex);
	}

}
