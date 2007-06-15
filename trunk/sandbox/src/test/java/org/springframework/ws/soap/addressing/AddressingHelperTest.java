/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.ws.soap.addressing;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;

import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.w3c.dom.Element;

public class AddressingHelperTest extends AbstractWsAddressingTestCase {

    private AddressingHelper helper;

    private Element headerElement;

    protected void onSetUp() throws Exception {
        helper = new AddressingHelper(new WsAddressing200408());
        SaajSoapMessage message = loadSaajMessage("request-200408.xml");
        headerElement = message.getSaajMessage().getSOAPHeader();
    }

    public void testAddMessageHeaderRequiredFault12() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage saajMessage = messageFactory.createMessage();
        SaajSoapMessage message = new SaajSoapMessage(saajMessage);
        helper.addMessageHeaderRequiredFault(message);

        saajMessage.writeTo(System.out);
        System.out.println();
    }

    public void testAddDestinationUnreachableFault() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage saajMessage = messageFactory.createMessage();
        SaajSoapMessage message = new SaajSoapMessage(saajMessage);
        helper.addDestinationUnreachableFault(message);
        saajMessage.writeTo(System.out);
        System.out.println();
    }

    public void testAddActionNotSupportedFault() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage saajMessage = messageFactory.createMessage();
        SaajSoapMessage message = new SaajSoapMessage(saajMessage);
        helper.addActionNotSupportedFault(message, "myAction");
        saajMessage.writeTo(System.out);
        System.out.println();
    }
}