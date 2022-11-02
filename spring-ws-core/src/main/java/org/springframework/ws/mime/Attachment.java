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

package org.springframework.ws.mime;

import jakarta.activation.DataHandler;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents an attachment to a {@link org.springframework.ws.mime.MimeMessage}
 *
 * @author Arjen Poutsma
 * @see MimeMessage#getAttachments()
 * @see MimeMessage#addAttachment
 * @since 1.0.0
 */
public interface Attachment {

	/**
	 * Returns the content identifier of the attachment.
	 *
	 * @return the content id, or {@code null} if empty or not defined
	 */
	String getContentId();

	/**
	 * Returns the content type of the attachment.
	 *
	 * @return the content type, or {@code null} if empty or not defined
	 */
	String getContentType();

	/**
	 * Return an {@code InputStream} to read the contents of the attachment from. The user is responsible for closing the
	 * stream.
	 *
	 * @return the contents of the file as stream, or an empty stream if empty
	 * @throws IOException in case of access I/O errors
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Returns the size of the attachment in bytes. Returns {@code -1} if the size cannot be determined.
	 *
	 * @return the size of the attachment, {@code 0} if empty, or {@code -1} if the size cannot be determined
	 */
	long getSize();

	/**
	 * Returns the data handler of the attachment.
	 *
	 * @return the data handler of the attachment
	 */
	DataHandler getDataHandler();
}
