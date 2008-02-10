package org.springframework.ws.soap.security.wss4j;

import java.util.Iterator;
import java.util.Properties;
import javax.xml.namespace.QName;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.wss4j.callback.SimplePasswordValidationCallbackHandler;

public abstract class Wss4jMessageInterceptorHeaderTestCase extends Wss4jTestCase {

    private Wss4jSecurityInterceptor interceptor;

    protected void onSetup() throws Exception {
        Properties users = new Properties();
        users.setProperty("Bert", "Ernie");
        interceptor = new Wss4jSecurityInterceptor();
        interceptor.setValidateRequest(true);
        interceptor.setSecureResponse(true);
        interceptor.setValidationActions("UsernameToken");
        SimplePasswordValidationCallbackHandler callbackHandler = new SimplePasswordValidationCallbackHandler();
        callbackHandler.setUsers(users);
        interceptor.setValidationCallbackHandler(callbackHandler);
        interceptor.afterPropertiesSet();
    }

    public void testValidateUsernameTokenPlainText() throws Exception {
        SoapMessage message = loadMessage("usernameTokenPlainTextWithHeaders-soap.xml");
        MessageContext messageContext = new DefaultMessageContext(message, getMessageFactory());
        interceptor.validateMessage(message, messageContext);
        Object result = getMessage(message);
        assertNotNull("No result returned", result);

        for (Iterator i = message.getEnvelope().getHeader()
                .examineAllHeaderElements(); i.hasNext();) {
            SoapHeaderElement element = (SoapHeaderElement) i.next();
            QName name = element.getName();
            if (name
                    .getNamespaceURI()
                    .equals("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd")) {
                fail("Security Header not removed");
            }

        }

        assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security",
                getDocument(message));
        assertXpathExists("header1 not found", "/SOAP-ENV:Envelope/SOAP-ENV:Header/header1", getDocument(message));
        assertXpathExists("header2 not found", "/SOAP-ENV:Envelope/SOAP-ENV:Header/header2", getDocument(message));

    }
}
