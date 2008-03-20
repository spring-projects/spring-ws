package org.springframework.ws.soap.security.wss4j;

import org.w3c.dom.Document;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;

public abstract class Wss4jMessageInterceptorUsernameTokenSignatureTestCase extends Wss4jTestCase {

    public void testAddUsernameTokenSignature() throws Exception {
        Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
        interceptor.setSecurementActions("UsernameTokenSignature");
        interceptor.setSecurementUsername("Bert");
        interceptor.setSecurementPassword("Ernie");
        interceptor.afterPropertiesSet();
        SoapMessage message = loadMessage("empty-soap.xml");
        MessageContext context = getMessageContext(message);
        interceptor.secureMessage(message, context);

        Document doc = getDocument(message);
        assertXpathEvaluatesTo("Invalid Username", "Bert",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Username/text()", doc);
        assertXpathExists("Invalid Password",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Password[@Type='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest']/text()",
                doc);
    }
}
