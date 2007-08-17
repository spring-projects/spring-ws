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

import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.addressing.messageid.UuidMessageIdProvider;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

public class StatefulAddressingInterceptorTest extends AbstractWsAddressingTestCase {

    private StatefulAddressingInterceptor interceptor;

    private SaajSoapMessage request;

    private SOAPMessage saajRequest;

    private MessageContext messageContext;

    private WsAddressing200408 version;

    protected void onSetUp() throws Exception {
        version = new WsAddressing200408();
        MessageAddressingProperties map = new MessageAddressingProperties("mailto:joe@fabrikam123.example",
                new EndpointReference("http://business456.example/client1"), "http://fabrikam123.example/mail/Delete",
                "uuid:aaaabbbb-cccc-dddd-eeee-ffffffffffff");
        interceptor =
                new StatefulAddressingInterceptor(new AddressingHelper(version), new UuidMessageIdProvider(), map);
        request = loadSaajMessage("request-200408.xml");
        saajRequest = request.getSaajMessage();
        messageContext = new DefaultMessageContext(request, new SaajSoapMessageFactory(messageFactory));
    }

    public void testUnderstands() throws Exception {
        Iterator iterator = request.getSoapHeader()
                .examineAllHeaderElements();
        SoapHeaderElement headerElement = (SoapHeaderElement) iterator.next();
        assertTrue("Interceptor does not understand header", interceptor.understands(headerElement));
    }

    public void testHandleRequestNormal() throws Exception {
        boolean result = interceptor.handleRequest(messageContext, null);
        assertTrue("Invalid result", result);
    }

    public void testHandleRequestToMissing() throws Exception {
        removeElement(version.getToName());
        boolean result = interceptor.handleRequest(messageContext, null);
        assertFalse("Invalid result", result);
        assertTrue("Response has no fault", messageContext.getResponse().hasFault());
    }

    public void testHandleRequestActionMissing() throws Exception {
        removeElement(version.getActionName());
        boolean result = interceptor.handleRequest(messageContext, null);
        assertFalse("Invalid result", result);
        assertTrue("Response has no fault", messageContext.getResponse().hasFault());
    }

    public void testHandleRequestMessageIdMissing() throws Exception {
        removeElement(version.getMessageIdName());
        boolean result = interceptor.handleRequest(messageContext, null);
        assertFalse("Invalid result", result);
        assertTrue("Response has no fault", messageContext.getResponse().hasFault());
    }

    public void testHandleResponse() throws Exception {
        interceptor.handleRequest(messageContext, null);
        interceptor.handleResponse(messageContext, null);
        messageContext.getResponse().writeTo(System.out);
    }

    public void testHandleFault() throws Exception {
        interceptor.handleRequest(messageContext, null);
        interceptor.handleFault(messageContext, null);
        messageContext.getResponse().writeTo(System.out);
    }

    private void removeElement(QName name) throws SOAPException {
        Iterator iterator = saajRequest.getSOAPHeader().getChildElements(name);
        while (iterator.hasNext()) {
            SOAPElement element = (SOAPElement) iterator.next();
            element.detachNode();
        }
    }

}