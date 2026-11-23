/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import java.io.IOException;
import java.io.InputStream;

import jakarta.activation.DataHandler;

import org.springframework.util.Assert;
import org.springframework.ws.mime.Attachment;

/**
 * Axiom-specific implementation of {@link org.springframework.ws.mime.Attachment}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class AxiomAttachment implements Attachment {

	private final DataHandler dataHandler;

	private final String contentId;

	AxiomAttachment(String contentId, DataHandler dataHandler) {
		Assert.notNull(contentId, "contentId must not be null");
		Assert.notNull(dataHandler, "dataHandler must not be null");
		this.contentId = contentId;
		this.dataHandler = dataHandler;
	}

	@Override
	public String getContentId() {
		return this.contentId;
	}

	@Override
	public String getContentType() {
		return this.dataHandler.getContentType();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return this.dataHandler.getInputStream();
	}

	@Override
	public long getSize() {
		// Axiom does not support getting the size of attachments.
		return -1;
	}

	@Override
	public DataHandler getDataHandler() {
		return this.dataHandler;
	}

}
