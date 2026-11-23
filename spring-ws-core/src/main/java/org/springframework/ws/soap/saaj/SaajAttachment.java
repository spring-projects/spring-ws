/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import java.io.IOException;
import java.io.InputStream;

import jakarta.activation.DataHandler;
import jakarta.xml.soap.AttachmentPart;
import jakarta.xml.soap.SOAPException;

import org.springframework.util.Assert;
import org.springframework.ws.mime.Attachment;

/**
 * SAAJ-specific implementation of the {@code Attachment} interface. Wraps a
 * {@link jakarta.xml.soap.AttachmentPart}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class SaajAttachment implements Attachment {

	private final AttachmentPart saajAttachment;

	SaajAttachment(AttachmentPart saajAttachment) {
		Assert.notNull(saajAttachment, "saajAttachment must not be null");
		this.saajAttachment = saajAttachment;
	}

	@Override
	public String getContentId() {
		return this.saajAttachment.getContentId();
	}

	@Override
	public String getContentType() {
		return this.saajAttachment.getContentType();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		try {
			return this.saajAttachment.getDataHandler().getInputStream();
		}
		catch (SOAPException ex) {
			throw new SaajAttachmentException(ex);
		}
	}

	@Override
	public long getSize() {
		try {
			return this.saajAttachment.getSize();
		}
		catch (SOAPException ex) {
			throw new SaajAttachmentException(ex);
		}
	}

	@Override
	public DataHandler getDataHandler() {
		try {
			return this.saajAttachment.getDataHandler();
		}
		catch (SOAPException ex) {
			throw new SaajAttachmentException(ex);
		}
	}

}
