package org.springframework.ws.soap.addressing;

import java.util.Iterator;

import org.easymock.MockControl;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.addressing.messageid.MessageIdProvider;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

public abstract class AbstractWsAddressingInterceptorTestCase extends AbstractWsAddressingTestCase {

    protected AbstractWsAddressingInterceptor interceptor;

    private MockControl providerControl;

    private MessageIdProvider providerMock;

    protected final void onSetUp() throws Exception {
        providerControl = MockControl.createControl(MessageIdProvider.class);
        providerMock = (MessageIdProvider) providerControl.getMock();
        interceptor = createInterceptor();
        interceptor.setMessageIdProvider(providerMock);
    }

    public void testUnderstands() throws Exception {
        SaajSoapMessage validRequest = loadSaajMessage(getTestPath() + "/valid.xml");
        Iterator iterator = validRequest.getSoapHeader().examineAllHeaderElements();
        providerControl.replay();
        while (iterator.hasNext()) {
            SoapHeaderElement headerElement = (SoapHeaderElement) iterator.next();
            assertTrue("Header [" + headerElement.getName() + " not understood",
                    interceptor.understands(headerElement));
        }
        providerControl.verify();
    }

    public void testHandleValidRequest() throws Exception {
        SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/valid.xml");
        MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(messageFactory));
        providerControl.replay();
        boolean result = interceptor.handleRequest(context, null);
        assertTrue("Valid request not handled", result);
        assertFalse("Message Context has response", context.hasResponse());
        providerControl.verify();
    }

    public void testHandleInvalidRequest() throws Exception {
        SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/invalid.xml");
        MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(messageFactory));
        providerControl.replay();
        boolean result = interceptor.handleRequest(context, null);
        assertFalse("Invalid request handled", result);
        assertTrue("Message Context has no response", context.hasResponse());
        SaajSoapMessage expectedResponse = loadSaajMessage(getTestPath() + "/response-invalid.xml");
        assertXMLEqual("Invalid response for message with invalid MAP", expectedResponse,
                (SaajSoapMessage) context.getResponse());
        providerControl.verify();
    }

    public void testHandleAnonymousReplyTo() throws Exception {
        SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/anonymous.xml");
        MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(messageFactory));
        SaajSoapMessage response = (SaajSoapMessage) context.getResponse();
        String messageId = "uid:1234";
        providerControl.expectAndReturn(providerMock.getMessageId(response), messageId);
        providerControl.replay();
        boolean result = interceptor.handleResponse(context, null);
        assertTrue("Anonymous request not handled", result);
        SaajSoapMessage expectedResponse = loadSaajMessage(getTestPath() + "/response-anonymous.xml");
        assertXMLEqual("Invalid response for message with invalid MAP", expectedResponse,
                (SaajSoapMessage) context.getResponse());
        providerControl.verify();
    }

    public void testHandleNoneReplyTo() throws Exception {
        SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/none.xml");
        MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(messageFactory));
        providerControl.replay();
        boolean result = interceptor.handleResponse(context, null);
        assertFalse("None request handled", result);
        providerControl.verify();
    }

    public void testHandleOutOfBandReplyTo() throws Exception {
        SaajSoapMessage valid = loadSaajMessage(getTestPath() + "/valid.xml");
        MessageContext context = new DefaultMessageContext(valid, new SaajSoapMessageFactory(messageFactory));
        SaajSoapMessage response = (SaajSoapMessage) context.getResponse();
        String messageId = "uid:1234";
        providerControl.expectAndReturn(providerMock.getMessageId(response), messageId);
        providerControl.replay();
        boolean result = interceptor.handleResponse(context, null);
        assertFalse("Out of Band request handled", result);
        providerControl.verify();
    }

    protected abstract AbstractWsAddressingInterceptor createInterceptor();

    protected abstract String getTestPath();

}
