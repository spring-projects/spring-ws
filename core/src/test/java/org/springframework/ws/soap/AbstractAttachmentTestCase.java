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
import javax.xml.soap.SOAPException;

import junit.framework.TestCase;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.FileCopyUtils;

public abstract class AbstractAttachmentTestCase extends TestCase {

    private Attachment attachment;

    private static byte[] CONTENTS;

    private static final String CONTENT_TYPE = "text/plain";

    protected final void setUp() throws Exception {
        CONTENTS = "attachment contents".getBytes("UTF-8");
        SoapMessage message = createMessage();
        attachment = message.addAttachment(new ByteArrayResource(CONTENTS), CONTENT_TYPE);
    }

    protected abstract SoapMessage createMessage() throws Exception;

    public void testGetSize() throws SOAPException {
        assertTrue("Invalid size", attachment.getSize() > 0);
    }

    public void testContentId() {
        String id = "123";
        attachment.setId(id);
        assertEquals("Invalid content id", id, attachment.getId());
    }

    public void testGetContentType() {
        assertEquals("Invalid content type", CONTENT_TYPE, attachment.getContentType());
    }

    public void testGetInputStream() throws IOException {
        InputStream inputStream = attachment.getInputStream();
        assertNotNull("Invalid input stream returned", inputStream);
        byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
        assertEquals("Invalid size", CONTENTS.length, bytes.length);
    }

}
