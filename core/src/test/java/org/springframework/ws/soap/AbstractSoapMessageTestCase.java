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

import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.ws.mime.AbstractMimeMessageTestCase;
import org.springframework.ws.mime.MimeMessage;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;
import org.xml.sax.SAXParseException;

public abstract class AbstractSoapMessageTestCase extends AbstractMimeMessageTestCase {

    protected SoapMessage soapMessage;

    protected MimeMessage createMimeMessage() throws Exception {
        soapMessage = createSoapMessage();
        return soapMessage;
    }

    protected abstract SoapMessage createSoapMessage() throws Exception;

    public void testValidate() throws Exception {
        XmlValidator validator =
                XmlValidatorFactory.createValidator(getSoapSchemas(), XmlValidatorFactory.SCHEMA_W3C_XML);
        SAXParseException[] errors = validator.validate(soapMessage.getEnvelope().getSource());
        if (errors.length > 0) {
            fail(StringUtils.arrayToCommaDelimitedString(errors));
        }
    }

    public void testSoapAction() throws Exception {
        String soapAction = "SoapAction";
        soapMessage.setSoapAction(soapAction);
        assertEquals("Invalid SOAP Action", soapAction, soapMessage.getSoapAction());
    }

    protected abstract Resource[] getSoapSchemas();

    public abstract void testGetVersion() throws Exception;

    public abstract void testWriteToTransportOutputStream() throws Exception;

    public abstract void testWriteToTransportResponseAttachment() throws Exception;
}
