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

import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.custommonkey.xmlunit.XMLTestCase;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.util.FileCopyUtils;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public abstract class AbstractSoapMessageTestCase extends XMLTestCase {

    protected SoapMessage soapMessage;

    protected Transformer transformer;

    protected final void setUp() throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        soapMessage = createSoapMessage();
    }

    protected abstract SoapMessage createSoapMessage() throws Exception;

    public void testPayload() throws Exception {
        String payload = "<payload/>";
        StringSource contents = new StringSource(payload);
        transformer.transform(contents, soapMessage.getPayloadResult());
        StringResult result = new StringResult();
        transformer.transform(soapMessage.getPayloadSource(), result);
        assertXMLEqual("Invalid payload", payload, result.toString());
    }

    public void testAttachments() throws Exception {
        String contents = "contents";
        String contentType = "text/plain";
        InputStreamSource inputStreamSource = new ByteArrayResource(contents.getBytes("UTF-8"));
        soapMessage.addAttachment(inputStreamSource, contentType);
        Iterator iterator = soapMessage.getAttachments();
        assertNotNull("Attachment iterator is null", iterator);
        assertTrue("Attachment iterator has no elements", iterator.hasNext());
        Attachment attachment = (Attachment) iterator.next();
        assertEquals("Invalid conent-id", contentType, attachment.getContentType());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        FileCopyUtils.copy(attachment.getInputStream(), os);
        String result = new String(os.toByteArray(), "UTF-8");
        assertEquals("Invalid contents", contents, result);
        assertFalse("Attachment iterator has too many elements", iterator.hasNext());
    }


}
