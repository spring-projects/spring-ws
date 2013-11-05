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

package org.springframework.ws.soap.saaj;

import java.util.Locale;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.soap11.AbstractSoap11BodyTestCase;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SaajSoap11BodyTest extends AbstractSoap11BodyTestCase {

    @Override
    protected SoapBody createSoapBody() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage saajMessage = messageFactory.createMessage();
        return new SaajSoap11Body(saajMessage.getSOAPPart().getEnvelope().getBody(), true);
    }

    @Test
    public void testLangAttributeOnSoap11FaultString() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage saajMessage = messageFactory.createMessage();

        SOAPBody saajSoapBody = saajMessage.getSOAPPart().getEnvelope().getBody();
        SaajSoap11Body soapBody = new SaajSoap11Body(saajSoapBody, true);

        soapBody.addClientOrSenderFault("Foo", Locale.ENGLISH);
        assertNotNull("No Language set", saajSoapBody.getFault().getFaultStringLocale());

        saajSoapBody = saajMessage.getSOAPPart().getEnvelope().getBody();
        soapBody = new SaajSoap11Body(saajSoapBody, false);

        soapBody.addClientOrSenderFault("Foo", Locale.ENGLISH);
        assertNull("Language set", saajSoapBody.getFault().getFaultStringLocale());
    }


}
