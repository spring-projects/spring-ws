/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.soap;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents an attachment to a <code>SoapMessage</code>.
 *
 * @author Arjen Poutsma
 * @see SoapMessage#getAttachments()
 * @see SoapMessage#addAttachment(java.io.File)
 * @see SoapMessage#addAttachment(org.springframework.core.io.InputStreamSource, String)
 */
public interface Attachment {

    /**
     * Returns the identifier of the attachment. Depending on the implementation used, this may be a MIME
     * <code>Content-Id</code> header, a DIME ID, etc.
     *
     * @return the attachment identifier, or <code>null</code> if empty or not defined
     */
    String getId();

    /**
     * Sets the identifier of the attachment. Depending on the implementation used, this may be a MIME
     * <code>Content-Id</code> header, a DIME ID, etc.
     *
     * @param id the new attachment identifier, or <code>null</code> if empty or not defined
     */
    void setId(String id);

    /**
     * Returns the content type of the attachment.
     *
     * @return the content type, or <code>null</code> if empty or not defined
     */
    String getContentType();

    /**
     * Return an <code>InputStream</code> to read the contents of the attachment from. The user is responsible for
     * closing the stream.
     *
     * @return the contents of the file as stream, or an empty stream if empty
     * @throws IOException in case of access I/O errors
     */
    InputStream getInputStream() throws IOException;

    /**
     * Returns the size of the attachment in bytes.
     *
     * @return the size of the attachment, or <code>0</code> if empty
     */
    long getSize();
}
