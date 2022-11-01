/*
 * Copyright 2005-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.axiom;

import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;

import org.springframework.util.Assert;
import org.springframework.ws.mime.Attachment;

/**
 * Axiom-specific implementation of {@link org.springframework.ws.mime.Attachment}
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class AxiomAttachment implements Attachment {

	private final DataHandler dataHandler;

	private final String contentId;

	public AxiomAttachment(String contentId, DataHandler dataHandler) {
		Assert.notNull(contentId, "contentId must not be null");
		Assert.notNull(dataHandler, "dataHandler must not be null");
		this.contentId = contentId;
		this.dataHandler = dataHandler;
	}

	@Override
	public String getContentId() {
		return contentId;
	}

	@Override
	public String getContentType() {
		return dataHandler.getContentType();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return dataHandler.getInputStream();
	}

	@Override
	public long getSize() {
		// Axiom does not support getting the size of attachments.
		return -1;
	}

	@Override
	public DataHandler getDataHandler() {
		return dataHandler;
	}
}
