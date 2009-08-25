/*
 * Copyright 2002-2009 the original author or authors.
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

package org.springframework.ws.soap.soap12;

import java.io.ByteArrayOutputStream;

import junit.framework.Assert;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.ws.soap.AbstractSoapMessageTestCase;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.transport.MockTransportOutputStream;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.xml.transform.StringSource;

public abstract class AbstractSoap12MessageTestCase extends AbstractSoapMessageTestCase {

    public void testGetVersion() throws Exception {
        Assert.assertEquals("Invalid SOAP version", SoapVersion.SOAP_12, soapMessage.getVersion());
    }

    protected final Resource[] getSoapSchemas() {
        return new Resource[]{new ClassPathResource("xml.xsd", AbstractSoap12MessageTestCase.class),
                new ClassPathResource("soap12.xsd", AbstractSoap12MessageTestCase.class)};
    }

    public void testWriteToTransportOutputStream() throws Exception {
        SoapBody body = soapMessage.getSoapBody();
        String payload = "<payload xmlns='http://www.springframework.org' />";
        transformer.transform(new StringSource(payload), body.getPayloadResult());

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MockTransportOutputStream tos = new MockTransportOutputStream(bos);
        String soapAction = "http://springframework.org/spring-ws/Action";
        soapMessage.setSoapAction(soapAction);
        soapMessage.writeTo(tos);
        String result = bos.toString("UTF-8");
        assertXMLEqual(
                "<Envelope xmlns='http://www.w3.org/2003/05/soap-envelope'><Body><payload xmlns='http://www.springframework.org' /></Body></Envelope>",
                result);
        String contentType = (String) tos.getHeaders().get(TransportConstants.HEADER_CONTENT_TYPE);
        assertTrue("Invalid Content-Type set", contentType.indexOf(SoapVersion.SOAP_12.getContentType()) != -1);
        assertNull(TransportConstants.HEADER_SOAP_ACTION + " header must not be found",
                tos.getHeaders().get(TransportConstants.HEADER_SOAP_ACTION));
        assertTrue("Invalid Content-Type set", contentType.indexOf(soapAction) != -1);
        String resultAccept = (String) tos.getHeaders().get("Accept");
        assertNotNull("Invalid accept header", resultAccept);
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
        assertTrue("Content-Type for attachment message does not contains type=\"application/soap+xml\"",
                contentType.indexOf("type=\"application/soap+xml\"") != -1);
    }

}
