package org.springframework.ws.soap.security.wss4j;

import java.lang.reflect.Field;

import org.w3c.dom.Document;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;

public abstract class Wss4jMessageInterceptorTimestampTestCase extends Wss4jTestCase {

    public void testAddTimestamp() throws Exception {
        Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
        interceptor.setSecurementActions("Timestamp");
        interceptor.afterPropertiesSet();
        SoapMessage message = loadMessage("empty-soap.xml");
        MessageContext context = getMessageContext(message);
        interceptor.secureMessage(message, context);
        Document document = getDocument(message);
        assertXpathExists("timestamp header not found",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsu:Timestamp", document);
    }

    public void testValidateTimestamp() throws Exception {
        Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
        interceptor.setValidationActions("Timestamp");
        interceptor.afterPropertiesSet();
        SoapMessage message = getMessageWithTimestamp();

        MessageContext context = new DefaultMessageContext(message, getMessageFactory());
        interceptor.validateMessage(message, context);
        assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security",
                getDocument(message));
    }

    public void testValidateTimestampWithTtl() throws Exception {
        Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor() {
            public void setTimeToLive(int t) {
                try {
                    Field ttl = Wss4jSecurityInterceptor.class
                            .getDeclaredField("timeToLive");
                    ttl.setAccessible(true);
                    ttl.set(this, new Integer(t));

                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        interceptor.setValidationActions("Timestamp");
        interceptor.setTimeToLive(-10);
        interceptor.setTimestampStrict(true);
        interceptor.afterPropertiesSet();
        SoapMessage message = getMessageWithTimestamp();
        MessageContext context = new DefaultMessageContext(message, getMessageFactory());

        try {
            interceptor.validateMessage(message, context);
        }
        catch (Wss4jSecurityValidationException ex) {
            // expected
            return;
        }
        fail("Time to live validation failed");
    }

    private SoapMessage getMessageWithTimestamp() throws Exception {
        Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
        interceptor.setSecurementActions("Timestamp");
        interceptor.afterPropertiesSet();
        SoapMessage message = loadMessage("empty-soap.xml");
        MessageContext context = getMessageContext(message);
        interceptor.secureMessage(message, context);
        return message;
    }
}
