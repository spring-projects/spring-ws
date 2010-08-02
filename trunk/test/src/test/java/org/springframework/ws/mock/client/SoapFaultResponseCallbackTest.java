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

package org.springframework.ws.mock.client;

import java.io.IOException;
import java.util.Locale;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.soap11.Soap11Fault;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SoapFaultResponseCallbackTest {

    private SoapMessage response;

    @Before
    public void createResponse() throws SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage saajResponse = messageFactory.createMessage();
        this.response = new SaajSoapMessage(saajResponse);
    }

    @Test
    public void clientOrSenderFault() throws IOException {
        String faultString = "Foo";
        SoapFaultResponseCreator callback =
                SoapFaultResponseCreator.createClientOrSenderFault(faultString, Locale.ENGLISH);

        callback.doWithResponse(null, null, response);

        assertTrue("Response has no fault", response.hasFault());
        Soap11Fault soapFault = (Soap11Fault) response.getSoapBody().getFault();
        assertEquals("Response has invalid fault code", SoapVersion.SOAP_11.getClientOrSenderFaultName(),
                soapFault.getFaultCode());
        assertEquals("Response has invalid fault string", faultString, soapFault.getFaultStringOrReason());
        assertEquals("Response has invalid fault locale", Locale.ENGLISH, soapFault.getFaultStringLocale());
    }

    @Test
    public void mustUnderstandFault() throws IOException {
        String faultString = "Foo";
        SoapFaultResponseCreator callback =
                SoapFaultResponseCreator.createMustUnderstandFault(faultString, Locale.ENGLISH);

        callback.doWithResponse(null, null, response);

        assertTrue("Response has no fault", response.hasFault());
        Soap11Fault soapFault = (Soap11Fault) response.getSoapBody().getFault();
        assertEquals("Response has invalid fault code", SoapVersion.SOAP_11.getMustUnderstandFaultName(),
                soapFault.getFaultCode());
        assertEquals("Response has invalid fault string", faultString, soapFault.getFaultStringOrReason());
        assertEquals("Response has invalid fault locale", Locale.ENGLISH, soapFault.getFaultStringLocale());
    }

    @Test
    public void serverOrReceiverFault() throws IOException {
        String faultString = "Foo";
        SoapFaultResponseCreator callback =
                SoapFaultResponseCreator.createServerOrReceiverFault(faultString, Locale.ENGLISH);

        callback.doWithResponse(null, null, response);

        assertTrue("Response has no fault", response.hasFault());
        Soap11Fault soapFault = (Soap11Fault) response.getSoapBody().getFault();
        assertEquals("Response has invalid fault code", SoapVersion.SOAP_11.getServerOrReceiverFaultName(),
                soapFault.getFaultCode());
        assertEquals("Response has invalid fault string", faultString, soapFault.getFaultStringOrReason());
        assertEquals("Response has invalid fault locale", Locale.ENGLISH, soapFault.getFaultStringLocale());
    }

    @Test
    public void versionMismatchFault() throws IOException {
        String faultString = "Foo";
        SoapFaultResponseCreator callback =
                SoapFaultResponseCreator.createVersionMismatchFault(faultString, Locale.ENGLISH);

        callback.doWithResponse(null, null, response);

        assertTrue("Response has no fault", response.hasFault());
        Soap11Fault soapFault = (Soap11Fault) response.getSoapBody().getFault();
        assertEquals("Response has invalid fault code", SoapVersion.SOAP_11.getVersionMismatchFaultName(),
                soapFault.getFaultCode());
        assertEquals("Response has invalid fault string", faultString, soapFault.getFaultStringOrReason());
        assertEquals("Response has invalid fault locale", Locale.ENGLISH, soapFault.getFaultStringLocale());
    }

}
