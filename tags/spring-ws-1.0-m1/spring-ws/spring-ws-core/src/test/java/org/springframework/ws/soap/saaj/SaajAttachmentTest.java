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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import junit.framework.TestCase;

import org.springframework.util.FileCopyUtils;
import org.springframework.ws.soap.Attachment;

/**
 * @author Arjen Poutsma
 */
public class SaajAttachmentTest extends TestCase {

    private static final String CONTENTS = "attachment contents";

    private static final String CONTENT_ID = "contentId";

    private AttachmentPart saajAttachment;

    private Attachment attachment;

    protected void setUp() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage saajMessage = messageFactory.createMessage();
        this.saajAttachment = saajMessage.createAttachmentPart(CONTENTS, "text/plain");
        this.saajAttachment.setContentId(CONTENT_ID);
        saajMessage.addAttachmentPart(saajAttachment);
        SaajSoapMessage saajSoapMessage = new SaajSoapMessage(saajMessage);
        this.attachment = saajSoapMessage.getAttachment(CONTENT_ID);
    }

    public void testGetSize() throws SOAPException {
        assertEquals("Invalid size", saajAttachment.getSize(), attachment.getSize());
    }

    public void testGetContentId() {
        assertEquals("Invalid content id", CONTENT_ID, attachment.getId());
    }

    public void testGetContentType() {
        assertEquals("Invalid content type", "text/plain", attachment.getContentType());
    }

    public void testGetInputStream() throws IOException {
        InputStream inputStream = attachment.getInputStream();
        assertNotNull("Invalid input stream returned", inputStream);
        byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
        assertEquals("Invalid size", CONTENTS.getBytes().length, bytes.length);
    }

    public void testGetEmptyInputStream() throws IOException {
        saajAttachment.clearContent();
        InputStream inputStream = attachment.getInputStream();
        assertNotNull("Invalid input stream returned", inputStream);
        byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
        assertEquals("Invalid size", 0, bytes.length);
    }
}
