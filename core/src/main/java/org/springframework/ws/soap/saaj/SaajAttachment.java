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

package org.springframework.ws.soap.saaj;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;

import org.springframework.util.Assert;
import org.springframework.ws.soap.Attachment;

/**
 * SAAJ-specific implementation of the <code>Attachment</code> interface. Wraps a {@link
 * javax.xml.soap.AttachmentPart}.
 *
 * @author Arjen Poutsma
 */
class SaajAttachment implements Attachment {

    private final AttachmentPart saajAttachment;

    public SaajAttachment(AttachmentPart saajAttachment) {
        Assert.notNull(saajAttachment, "saajAttachment must not be null");
        this.saajAttachment = saajAttachment;
    }

    public String getId() {
        return saajAttachment.getContentId();
    }

    public void setId(String id) {
        saajAttachment.setContentId(id);
    }

    public String getContentType() {
        return saajAttachment.getContentType();
    }

    public InputStream getInputStream() throws IOException {
        try {
            return saajAttachment.getDataHandler().getInputStream();
        }
        catch (SOAPException e) {
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    public long getSize() {
        try {
            int result = saajAttachment.getSize();
            // SAAJ returns -1 when the size cannot be determined
            return result != -1 ? result : 0;
        }
        catch (SOAPException ex) {
            throw new SaajAttachmentException(ex);
        }
    }

}
