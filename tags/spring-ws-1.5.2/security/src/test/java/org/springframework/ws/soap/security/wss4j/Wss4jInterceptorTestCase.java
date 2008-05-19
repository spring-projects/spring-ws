package org.springframework.ws.soap.security.wss4j;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.WsSecuritySecurementException;
import org.springframework.ws.soap.security.WsSecurityValidationException;

public abstract class Wss4jInterceptorTestCase extends Wss4jTestCase {

    public void testhandleRequest() throws Exception {
        SoapMessage request = loadMessage("empty-soap.xml");
        final Object requestMessage = getMessage(request);
        SoapMessage validatedRequest = loadMessage("empty-soap.xml");
        final Object validatedRequestMessage = getMessage(validatedRequest);
        Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor() {
            protected void secureMessage(SoapMessage soapMessage, MessageContext messageContext)
                    throws WsSecuritySecurementException {
                fail("secure not expected");
            }

            protected void validateMessage(SoapMessage soapMessage, MessageContext messageContext)
                    throws WsSecurityValidationException {
                assertEquals("Invalid message", requestMessage, getMessage(soapMessage));
                setMessage(soapMessage, validatedRequestMessage);
            }
        };
        MessageContext context = new DefaultMessageContext(request, getMessageFactory());
        interceptor.handleRequest(context, null);
        assertEquals("Invalid request", validatedRequestMessage, getMessage((SoapMessage) context.getRequest()));
    }

    public void testhandleResponse() throws Exception {
        SoapMessage securedResponse = loadMessage("empty-soap.xml");
        final Object securedResponseMessage = getMessage(securedResponse);

        Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor() {

            protected void secureMessage(SoapMessage soapMessage, MessageContext messageContext)
                    throws WsSecuritySecurementException {
                setMessage(soapMessage, securedResponseMessage);
            }

            protected void validateMessage(SoapMessage soapMessage, MessageContext messageContext)
                    throws WsSecurityValidationException {
                fail("validate not expected");
            }

        };
        SoapMessage request = loadMessage("empty-soap.xml");
        MessageContext context = new DefaultMessageContext(request, getMessageFactory());
        context.getResponse();
        interceptor.handleResponse(context, null);
        assertEquals("Invalid response", securedResponseMessage, getMessage((SoapMessage) context.getResponse()));
    }

}
