package org.springframework.ws.soap.security.wss4j;

import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.Merlin;
import org.w3c.dom.Document;

import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.wss4j.support.CryptoFactoryBean;

public abstract class Wss4jMessageInterceptorX509TestCase extends Wss4jTestCase {

    protected Wss4jSecurityInterceptor interceptor;

    protected void onSetup() throws Exception {
        interceptor = new Wss4jSecurityInterceptor();
        interceptor.setSecurementActions("Signature");
        interceptor.setValidationActions("Signature");
        CryptoFactoryBean cryptoFactoryBean = new CryptoFactoryBean();
        cryptoFactoryBean.setCryptoProvider(Merlin.class);
        cryptoFactoryBean.setKeyStoreType("jceks");
        cryptoFactoryBean.setKeyStorePassword("123456");
        cryptoFactoryBean.setKeyStoreLocation(new ClassPathResource("private.jks"));

        cryptoFactoryBean.afterPropertiesSet();
        interceptor.setSecurementSignatureCrypto((Crypto) cryptoFactoryBean
                .getObject());
        interceptor.setValidationSignatureCrypto((Crypto) cryptoFactoryBean
                .getObject());
        interceptor.afterPropertiesSet();

    }

    public void testAddCertificate() throws Exception {

        interceptor.setSecurementPassword("123456");
        interceptor.setSecurementUsername("rsaKey");
        SoapMessage message = loadMessage("empty-soap.xml");
        MessageContext messageContext = getMessageContext(message);

        interceptor.setSecurementSignatureKeyIdentifier("DirectReference");

        interceptor.secureMessage(message, messageContext);
        Document document = getDocument(message);

        assertXpathExists("Absent BinarySecurityToken element",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:BinarySecurityToken", document);

        // lets verfiy the signature that we've just generated
        interceptor.validateMessage(message, messageContext);
    }

}
