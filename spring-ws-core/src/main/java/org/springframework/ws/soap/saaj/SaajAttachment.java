/*
 * Copyright 2005-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
