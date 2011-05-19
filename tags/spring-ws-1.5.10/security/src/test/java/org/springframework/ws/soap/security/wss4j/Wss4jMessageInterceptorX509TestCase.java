/*
 * Copyright 2008 the original author or authors.
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
        SoapMessage message = loadSoap11Message("empty-soap.xml");
        MessageContext messageContext = getSoap11MessageContext(message);

        interceptor.setSecurementSignatureKeyIdentifier("DirectReference");

        interceptor.secureMessage(message, messageContext);
        Document document = getDocument(message);

        assertXpathExists("Absent BinarySecurityToken element",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:BinarySecurityToken", document);

        // lets verfiy the signature that we've just generated
        interceptor.validateMessage(message, messageContext);
    }

}
