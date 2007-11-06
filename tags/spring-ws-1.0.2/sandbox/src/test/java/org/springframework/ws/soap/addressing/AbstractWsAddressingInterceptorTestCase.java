/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package org.springframework.ws.soap.addressing;

import java.util.Iterator;

import org.easymock.MockControl;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.addressing.messageid.MessageIdStrategy;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;

public abstract class AbstractWsAddressingInterceptorTestCase extends AbstractWsAddressingTestCase {

    protected WsAddressingInterceptor interceptor;

    private MockControl strategyControl;

    private MessageIdStrategy strategyMock;

    protected final void onSetUp() throws Exception {
        strategyControl = MockControl.createControl(MessageIdStrategy.class);
        strategyMock = (MessageIdStrategy) strategyControl.getMock();
        strategyControl.expectAndDefaultReturn(strategyMock.isDuplicate(null), false);
        interceptor = new WsAddressingInterceptor(getVersion(), strategyMock, new WebServiceMessageSender[0]);
    }

    public void testUnderstands() throws Exception {
        SaajSoapMessage validRequest = loadSaajMessage(getTestPath() + "/valid.xml");
        Iterator iterator = validRequest.getSoapHeader().examineAllHeaderElements();
        strategyControl.replay();
        while (iterator.hasNext()) {
            SoapHeaderElement headerElement = (SoapHeaderElement) iterator.next();
            assertTrue("Header [" + headerElement.getName() + " not understood",
                    interceptor.understands(headerElement));
        }
        strategyControl.verify();
    }

    public void testHandleValidRequest() throws Exception {
        SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/valid.xml");
        MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(messageFactory));
        strategyControl.replay();
        boolean result = interceptor.handleRequest(context, null);
        assertTrue("Valid request not handled", result);
        assertFalse("Message Context has response", context.hasResponse());
        strategyControl.verify();
    }

    public void testHandleInvalidRequest() throws Exception {
        SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/invalid.xml");
        MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(messageFactory));
        strategyControl.replay();
        boolean result = interceptor.handleRequest(context, null);
        assertFalse("Invalid request handled", result);
        assertTrue("Message Context has no response", context.hasResponse());
        SaajSoapMessage expectedResponse = loadSaajMessage(getTestPath() + "/response-invalid.xml");
        assertXMLEqual("Invalid response for message with invalid MAP", expectedResponse,
                (SaajSoapMessage) context.getResponse());
        strategyControl.verify();
    }

    public void testHandleAnonymousReplyTo() throws Exception {
        SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/anonymous.xml");
        MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(messageFactory));
        SaajSoapMessage response = (SaajSoapMessage) context.getResponse();
        String messageId = "uid:1234";
        strategyControl.expectAndReturn(strategyMock.newMessageId(response), messageId);
        strategyControl.replay();
        boolean result = interceptor.handleResponse(context, null);
        assertTrue("Anonymous request not handled", result);
        SaajSoapMessage expectedResponse = loadSaajMessage(getTestPath() + "/response-anonymous.xml");
        assertXMLEqual("Invalid response for message with invalid MAP", expectedResponse,
                (SaajSoapMessage) context.getResponse());
        strategyControl.verify();
    }

    public void testHandleNoneReplyTo() throws Exception {
        SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/none.xml");
        MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(messageFactory));
        strategyControl.replay();
        boolean result = interceptor.handleResponse(context, null);
        assertFalse("None request handled", result);
        strategyControl.verify();
    }

    public void testHandleOutOfBandReplyTo() throws Exception {
        MockControl senderControl = MockControl.createControl(WebServiceMessageSender.class);
        WebServiceMessageSender senderMock = (WebServiceMessageSender) senderControl.getMock();

        interceptor =
                new WsAddressingInterceptor(getVersion(), strategyMock, new WebServiceMessageSender[]{senderMock});

        MockControl connectionControl = MockControl.createControl(WebServiceConnection.class);
        WebServiceConnection connectionMock = (WebServiceConnection) connectionControl.getMock();

        SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/valid.xml");
        MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(messageFactory));
        SaajSoapMessage response = (SaajSoapMessage) context.getResponse();

        String messageId = "uid:1234";
        strategyControl.expectAndReturn(strategyMock.newMessageId(response), messageId);

        String uri = "http://example.com/business/client1";
        senderControl.expectAndReturn(senderMock.supports(uri), true);
        senderControl.expectAndReturn(senderMock.createConnection(uri), connectionMock);
        connectionMock.send(response);
        connectionMock.close();

        strategyControl.replay();
        senderControl.replay();
        connectionControl.replay();

        boolean result = interceptor.handleResponse(context, null);
        assertFalse("Out of Band request handled", result);

        strategyControl.verify();
        senderControl.verify();
        connectionControl.verify();
    }

    protected abstract WsAddressingVersion getVersion();

    protected abstract String getTestPath();

}
