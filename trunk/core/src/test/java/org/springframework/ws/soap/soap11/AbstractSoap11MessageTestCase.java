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

package org.springframework.ws.soap.soap11;

import java.io.ByteArrayOutputStream;

import junit.framework.Assert;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.ws.soap.AbstractSoapMessageTestCase;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.transport.MockTransportOutputStream;

public abstract class AbstractSoap11MessageTestCase extends AbstractSoapMessageTestCase {

    protected final Resource[] getSoapSchemas() {
        return new Resource[]{new ClassPathResource("soap11.xsd", AbstractSoap11MessageTestCase.class)};
    }

    public void testGetVersion() throws Exception {
        Assert.assertEquals("Invalid SOAP version", SoapVersion.SOAP_11, soapMessage.getVersion());
    }

    public void testWriteToTransportOutputStream() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MockTransportOutputStream tos = new MockTransportOutputStream(bos);
        String soapAction = "http://springframework.org/spring-ws/Action";
        soapMessage.setSoapAction(soapAction);
        soapMessage.writeTo(tos);
        String result = bos.toString("UTF-8");
        assertXMLEqual("<Envelope xmlns='http://schemas.xmlsoap.org/soap/envelope/'><Body/></Envelope>", result);
        String contentType = (String) tos.getHeaders().get("Content-Type");
        assertTrue("Invalid Content-Type set", contentType.indexOf(SoapVersion.SOAP_11.getContentType()) != -1);
        String resultSoapAction = (String) tos.getHeaders().get("SOAPAction");
        assertEquals("Invalid soap action", "\"" + soapAction + "\"", resultSoapAction);
    }

    public void testWriteToTransportResponseAttachment() throws Exception {
        InputStreamSource inputStreamSource = new ByteArrayResource("contents".getBytes("UTF-8"));
        soapMessage.addAttachment("contentId", inputStreamSource, "text/plain");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MockTransportOutputStream tos = new MockTransportOutputStream(bos);
        soapMessage.writeTo(tos);
        String contentType = (String) tos.getHeaders().get("Content-Type");
        assertTrue("Content-Type for attachment message does not contains multipart/related",
                contentType.indexOf("multipart/related") != -1);
        assertTrue("Content-Type for attachment message does not contains type=\"text/xml\"",
                contentType.indexOf("type=\"text/xml\"") != -1);
    }


}
