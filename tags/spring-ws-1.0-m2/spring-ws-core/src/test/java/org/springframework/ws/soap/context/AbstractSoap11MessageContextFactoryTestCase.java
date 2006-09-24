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

import java.util.Properties;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mock.MockTransportContext;
import org.springframework.ws.soap.Attachment;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;

public abstract class AbstractSoap11MessageContextFactoryTestCase extends AbstractSoapMessageContextFactoryTestCase {

    public void testCreateContextNoAttachment() throws Exception {
        Properties headers = new Properties();
        headers.setProperty("Content-Type", "text/xml");
        headers.setProperty("SOAPAction", "\"Some-URI\"");
        MockTransportContext transportContext = createTransportContext(headers, "soap11.xml");

        MessageContext messageContext = contextFactory.createContext(transportContext);
        SoapMessage requestMessage = (SoapMessage) messageContext.getRequest();
        assertNotNull("Request null", requestMessage);
        assertEquals("Invalid soap version", SoapVersion.SOAP_11, requestMessage.getVersion());
    }

    public void testCreateContextAttachment() throws Exception {
        Properties headers = new Properties();
        headers.setProperty("Content-Type",
                "multipart/related; type=\"text/xml\"; boundary=\"----=_Part_0_11416420.1149699787554\"");
        MockTransportContext transportContext = createTransportContext(headers, "soap11-attachment.bin");

        MessageContext messageContext = contextFactory.createContext(transportContext);
        SoapMessage requestMessage = (SoapMessage) messageContext.getRequest();
        assertEquals("Invalid soap version", SoapVersion.SOAP_11, requestMessage.getVersion());
        Attachment attachment = requestMessage.getAttachment("interface21");
        assertNotNull("No attachment read", attachment);
    }

}
