/*
 * Copyright 2005-2010 the original author or authors.
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

import java.io.IOException;
import java.util.Iterator;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.AbstractWebServiceMessageTestCase;
import org.springframework.ws.WebServiceMessage;

public abstract class AbstractMimeMessageTestCase extends AbstractWebServiceMessageTestCase {

    protected MimeMessage mimeMessage;

    private Resource picture;

    private String contentId;

    private String contentType;

    @Override
    protected final WebServiceMessage createWebServiceMessage() throws Exception {
        mimeMessage = createMimeMessage();
        picture = new ClassPathResource("spring-ws.png", AbstractMimeMessageTestCase.class);
        contentId = "spring-ws";
        contentType = "image/png";
        return mimeMessage;
    }

    protected abstract MimeMessage createMimeMessage() throws Exception;

    public void testEmptyMessage() throws Exception {
        Iterator iterator = mimeMessage.getAttachments();
        assertFalse("Empty MimeMessage has attachments", iterator.hasNext());
    }

    public void testAddAttachment() throws Exception {
        Attachment attachment = mimeMessage.addAttachment(contentId, picture, contentType);
        testAttachment(attachment);
    }

    public void testGetAttachment() throws Exception {
        mimeMessage.addAttachment(contentId, picture, contentType);
        Attachment attachment = mimeMessage.getAttachment(contentId);
        assertNotNull("Not Attachment found", attachment);
        testAttachment(attachment);
    }

    public void testGetAttachments() throws Exception {
        mimeMessage.addAttachment(contentId, picture, contentType);
        Iterator iterator = mimeMessage.getAttachments();
        assertNotNull("Attachment iterator is null", iterator);
        assertTrue("Attachment iterator has no elements", iterator.hasNext());
        Attachment attachment = (Attachment) iterator.next();
        testAttachment(attachment);
        assertFalse("Attachment iterator has too many elements", iterator.hasNext());
    }

    private void testAttachment(Attachment attachment) throws IOException {
        assertEquals("Invalid content id", contentId, attachment.getContentId());
        assertEquals("Invalid content type", contentType, attachment.getContentType());
        assertTrue("Invalid size", attachment.getSize() != 0);
        byte[] contents = FileCopyUtils.copyToByteArray(attachment.getInputStream());
        assertTrue("No contents", contents.length > 0);
    }

}