package org.springframework.ws.soap.security.wss4j2;

import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.apache.wss4j.dom.WSConstants;
import org.junit.Test;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.w3c.dom.Document;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

public abstract class Wss4jMessageInterceptorSecurementCallbackTestCase extends Wss4jTestCase {
    protected Wss4jSecurityInterceptor interceptor;
    private static final String securementPassword = "Ernie";
    private static final String callBackPassword = "testPassword";

    @Override
    protected void onSetup() throws Exception {
        interceptor = new Wss4jSecurityInterceptor();
        interceptor.setSecurementCallbackHandler(new PasswordCallbackHandler());
        interceptor.setSecurementActions("UsernameToken");
        interceptor.setSecurementUsername("Bert");
        interceptor.setSecurementPassword(securementPassword);
        interceptor.setSecurementPasswordType(WSConstants.PW_TEXT);

        SoapMessage message = loadSoap11Message("empty-soap.xml");
        MessageContext messageContext = new DefaultMessageContext(message, getSoap11MessageFactory());
        interceptor.handleRequest(messageContext);
    }

    @Test
    public void testChangePassword() throws Exception {
        SoapMessage message = loadSoap11Message("empty-soap.xml");
        MessageContext messageContext = new DefaultMessageContext(message, getSoap11MessageFactory());
        interceptor.handleRequest(messageContext);
        Document document = getDocument(message);
        assertXpathEvaluatesTo("Right password", callBackPassword,
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Password[@Type='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText']/text()",
                document);
    }


    private class PasswordCallbackHandler implements CallbackHandler {

        @Override
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback clb : callbacks) {
                if (clb instanceof WSPasswordCallback) {
                    WSPasswordCallback wsPasswordCallback = (WSPasswordCallback) clb;

                    if (wsPasswordCallback.getUsage() == WSPasswordCallback.USERNAME_TOKEN) {
                        wsPasswordCallback.setPassword(callBackPassword);
                    } else {
                        wsPasswordCallback.setPassword(securementPassword);
                    }
                } else {
                    throw new UnsupportedCallbackException(clb);
                }
            }
        }
    }
}
