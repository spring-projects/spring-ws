/*
 * Copyright ${YEAR} the original author or authors.
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

package org.springframework.ws.soap.addressing.client;

import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.easymock.MockControl;

import org.springframework.ws.soap.addressing.AbstractWsAddressingTestCase;
import org.springframework.ws.soap.addressing.core.EndpointReference;
import org.springframework.ws.soap.addressing.messageid.MessageIdStrategy;
import org.springframework.ws.soap.addressing.version.AddressingVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.context.DefaultTransportContext;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;

public abstract class AbstractActionCallbackTestCase extends AbstractWsAddressingTestCase {

    private ActionCallback callback;

    private MockControl strategyControl;

    private MessageIdStrategy strategyMock;

    private MockControl connectionControl;

    private WebServiceConnection connectionMock;

    protected final void onSetUp() throws Exception {
        strategyControl = MockControl.createControl(MessageIdStrategy.class);
        strategyMock = (MessageIdStrategy) strategyControl.getMock();

        connectionControl = MockControl.createControl(WebServiceConnection.class);
        connectionMock = (WebServiceConnection) connectionControl.getMock();
        TransportContext transportContext = new DefaultTransportContext(connectionMock);
        TransportContextHolder.setTransportContext(transportContext);
    }

    protected void tearDown() throws Exception {
        TransportContextHolder.setTransportContext(null);
    }

    public void testValid() throws Exception {
        URI action = new URI("http://example.com/fabrikam/mail/Delete");
        URI to = new URI("mailto:fabrikam@example.com");
        callback = new ActionCallback(action, getVersion(), to);
        callback.setMessageIdStrategy(strategyMock);
        SaajSoapMessage message = createDeleteMessage();
        strategyControl
                .expectAndReturn(strategyMock.newMessageId(message), new URI("http://example.com/someuniquestring"));
        callback.setReplyTo(new EndpointReference(new URI("http://example.com/business/client1")));
        strategyControl.replay();
        connectionControl.replay();

        callback.doWithMessage(message);

        SaajSoapMessage expected = loadSaajMessage(getTestPath() + "/valid.xml");
        assertXMLEqual("Invalid message", expected, message);
        strategyControl.verify();
        connectionControl.verify();
    }

    public void testDefaults() throws Exception {
        URI action = new URI("http://example.com/fabrikam/mail/Delete");
        URI connectionUri = new URI("mailto:fabrikam@example.com");
        callback = new ActionCallback(action, getVersion());
        callback.setMessageIdStrategy(strategyMock);
        connectionControl.expectAndReturn(connectionMock.getUri(), connectionUri);
        connectionControl.replay();

        SaajSoapMessage message = createDeleteMessage();
        strategyControl
                .expectAndReturn(strategyMock.newMessageId(message), new URI("http://example.com/someuniquestring"));
        callback.setReplyTo(new EndpointReference(new URI("http://example.com/business/client1")));
        strategyControl.replay();

        callback.doWithMessage(message);

        SaajSoapMessage expected = loadSaajMessage(getTestPath() + "/valid.xml");
        assertXMLEqual("Invalid message", expected, message);
        strategyControl.verify();
        connectionControl.verify();
    }

    private SaajSoapMessage createDeleteMessage() throws SOAPException {
        SOAPMessage saajMessage = messageFactory.createMessage();
        SOAPBody saajBody = saajMessage.getSOAPBody();
        SOAPBodyElement delete = saajBody.addBodyElement(new QName("http://example.com/fabrikam", "Delete"));
        SOAPElement maxCount = delete.addChildElement(new QName("maxCount"));
        maxCount.setTextContent("42");
        return new SaajSoapMessage(saajMessage);
    }

    protected abstract AddressingVersion getVersion();

    protected abstract String getTestPath();

}