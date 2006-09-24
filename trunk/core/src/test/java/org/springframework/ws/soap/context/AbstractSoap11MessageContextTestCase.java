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

package org.springframework.ws.soap.context;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.ws.soap.SoapVersion;

public abstract class AbstractSoap11MessageContextTestCase extends AbstractSoapMessageContextTestCase {

    public void testWriteToTransportResponse() throws Exception {
        messageContext.getResponse(); // create response
        messageContext.sendResponse(transportResponse);
        assertXMLEqual("<Envelope xmlns='http://schemas.xmlsoap.org/soap/envelope/'><Header/><Body/></Envelope>",
                transportResponse.getContents());
        assertTrue("Invalid Content-Type set", transportResponse.getHeaders().getProperty("Content-Type")
                .indexOf(SoapVersion.SOAP_11.getContentType()) != -1);
    }

    public void testWriteToTransportResponseAttachment() throws Exception {
        InputStreamSource inputStreamSource = new ByteArrayResource("contents".getBytes("UTF-8"));
        messageContext.getSoapResponse().addAttachment(inputStreamSource, "text/plain");
        messageContext.sendResponse(transportResponse);
        assertTrue("Invalid Content-Type set",
                transportResponse.getHeaders().getProperty("Content-Type").indexOf("multipart/related") != -1);
        assertTrue("Invalid Content-Type set", transportResponse.getHeaders().getProperty("Content-Type")
                .indexOf(SoapVersion.SOAP_11.getContentType()) != -1);
    }

}
