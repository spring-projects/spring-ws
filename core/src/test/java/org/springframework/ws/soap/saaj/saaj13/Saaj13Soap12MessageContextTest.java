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

package org.springframework.ws.soap.saaj.saaj13;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;

import org.springframework.ws.soap.context.AbstractSoap12MessageContextTestCase;
import org.springframework.ws.soap.context.SoapMessageContext;
import org.springframework.ws.transport.TransportRequest;

public class Saaj13Soap12MessageContextTest extends AbstractSoap12MessageContextTestCase {

    protected SoapMessageContext createMessageContext(TransportRequest transportRequest) throws SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        return new Saaj13SoapMessageContext(messageFactory.createMessage(), transportRequest, messageFactory);
    }
}
