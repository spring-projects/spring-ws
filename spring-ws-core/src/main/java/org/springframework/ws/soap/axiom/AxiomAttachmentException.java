/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import org.springframework.ws.mime.AttachmentException;

/**
 * Exception thrown when a {@link AxiomAttachment} could not be accessed.
 *
 * @author Arjen Poutsma
 */
@SuppressWarnings("serial")
public class AxiomAttachmentException extends AttachmentException {

	public AxiomAttachmentException(String msg) {
		super(msg);
	}

	public AxiomAttachmentException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public AxiomAttachmentException(Throwable ex) {
		super(ex);
	}

}
