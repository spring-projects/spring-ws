/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import org.springframework.ws.mime.AttachmentException;

/**
 * Exception thrown when a {@link SaajAttachment} could not be accessed.
 *
 * @author Arjen Poutsma
 */
@SuppressWarnings("serial")
public class SaajAttachmentException extends AttachmentException {

	public SaajAttachmentException(String msg) {
		super(msg);
	}

	public SaajAttachmentException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public SaajAttachmentException(Throwable ex) {
		super(ex);
	}

}
