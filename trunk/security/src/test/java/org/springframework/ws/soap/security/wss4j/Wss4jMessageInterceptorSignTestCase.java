package org.springframework.ws.soap.security.wss4j;

import java.util.Properties;

import org.apache.ws.security.components.crypto.Crypto;
import org.w3c.dom.Document;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.wss4j.support.CryptoFactoryBean;

public abstract class Wss4jMessageInterceptorSignTestCase extends Wss4jTestCase {

    protected Wss4jSecurityInterceptor interceptor;

    protected void onSetup() throws Exception {
        interceptor = new Wss4jSecurityInterceptor();
        interceptor.setValidationActions("Signature");

        CryptoFactoryBean cryptoFactoryBean = new CryptoFactoryBean();
        Properties cryptoFactoryBeanConfig = new Properties();
        cryptoFactoryBeanConfig.setProperty("org.apache.ws.security.crypto.provider",
                "org.apache.ws.security.components.crypto.Merlin");
        cryptoFactoryBeanConfig.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", "jceks");
        cryptoFactoryBeanConfig.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", "123456");

        // from the class path
        cryptoFactoryBeanConfig.setProperty("org.apache.ws.security.crypto.merlin.file", "private.jks");
        cryptoFactoryBean.setConfiguration(cryptoFactoryBeanConfig);
        cryptoFactoryBean.afterPropertiesSet();
        interceptor.setValidationSignatureCrypto((Crypto) cryptoFactoryBean
                .getObject());
        interceptor.setSecurementSignatureCrypto((Crypto) cryptoFactoryBean
                .getObject());
        interceptor.afterPropertiesSet();

    }

    public void testValidateCertificate() throws Exception {
        SoapMessage message = loadMessage("signed-soap.xml");

        MessageContext messageContext = new DefaultMessageContext(message, getMessageFactory());
        interceptor.validateMessage(message, messageContext);
        Object result = getMessage(message);
        assertNotNull("No result returned", result);
        assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security",
                getDocument(message));
    }

    public void testValidateCertificateWithSignatureConfirmation() throws Exception {
        SoapMessage message = loadMessage("signed-soap.xml");
        MessageContext messageContext = getMessageContext(message);
        interceptor.setEnableSignatureConfirmation(true);
        interceptor.validateMessage(message, messageContext);
        WebServiceMessage response = messageContext.getResponse();
        interceptor.secureMessage(message, messageContext);
        assertNotNull("No result returned", response);
        Document document = getDocument((SoapMessage) response);
        assertXpathExists("Absent SignatureConfirmation element",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse11:SignatureConfirmation", document);
    }

    public void testSignResponse() throws Exception {
        interceptor.setSecurementActions("Signature");
        interceptor.setEnableSignatureConfirmation(false);
        interceptor.setSecurementPassword("123456");
        interceptor.setSecurementUsername("rsaKey");
        SoapMessage message = loadMessage("empty-soap.xml");
        MessageContext messageContext = getMessageContext(message);

        // interceptor.setSecurementSignatureKeyIdentifier("IssuerSerial");

        interceptor.secureMessage(message, messageContext);

        Document document = getDocument(message);
        assertXpathExists("Absent SignatureConfirmation element",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/ds:Signature", document);


    }

}
