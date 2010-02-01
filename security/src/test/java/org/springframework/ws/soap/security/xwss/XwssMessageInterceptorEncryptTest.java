/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.soap.security.xwss;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.soap.SOAPMessage;

import com.sun.xml.wss.impl.callback.DecryptionKeyCallback;
import com.sun.xml.wss.impl.callback.EncryptionKeyCallback;

import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.security.callback.AbstractCallbackHandler;

public class XwssMessageInterceptorEncryptTest extends AbstractXwssMessageInterceptorKeyStoreTestCase {

    public void testEncryptDefaultCertificate() throws Exception {
        interceptor.setPolicyConfiguration(new ClassPathResource("encrypt-config.xml", getClass()));
        CallbackHandler handler = new AbstractCallbackHandler() {

            @Override
            protected void handleInternal(Callback callback) {
                if (callback instanceof EncryptionKeyCallback) {
                    EncryptionKeyCallback keyCallback = (EncryptionKeyCallback) callback;
                    if (keyCallback.getRequest() instanceof EncryptionKeyCallback.AliasX509CertificateRequest) {
                        EncryptionKeyCallback.AliasX509CertificateRequest request =
                                (EncryptionKeyCallback.AliasX509CertificateRequest) keyCallback.getRequest();
                        assertNull("Invalid alias", request.getAlias());
                        request.setX509Certificate(certificate);
                    }
                    else {
                        fail("Unexpected request");
                    }
                }
                else {
                    fail("Unexpected callback");
                }
            }
        };
        interceptor.setCallbackHandler(handler);
        interceptor.afterPropertiesSet();
        SaajSoapMessage message = loadSaajMessage("empty-soap.xml");
        interceptor.secureMessage(message, null);
        SOAPMessage result = message.getSaajMessage();
        assertNotNull("No result returned", result);
        assertXpathExists("BinarySecurityToken does not exist",
                "SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:BinarySecurityToken", result);
        assertXpathExists("Signature does not exist",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/xenc:EncryptedKey", result);
    }

    public void testEncryptAlias() throws Exception {
        interceptor.setPolicyConfiguration(new ClassPathResource("encrypt-alias-config.xml", getClass()));
        CallbackHandler handler = new AbstractCallbackHandler() {

            @Override
            protected void handleInternal(Callback callback) {
                if (callback instanceof EncryptionKeyCallback) {
                    EncryptionKeyCallback keyCallback = (EncryptionKeyCallback) callback;
                    if (keyCallback.getRequest() instanceof EncryptionKeyCallback.AliasX509CertificateRequest) {
                        EncryptionKeyCallback.AliasX509CertificateRequest request =
                                (EncryptionKeyCallback.AliasX509CertificateRequest) keyCallback.getRequest();
                        assertEquals("Invalid alias", "alias", request.getAlias());
                        request.setX509Certificate(certificate);
                    }
                    else {
                        fail("Unexpected request");
                    }
                }
                else {
                    fail("Unexpected callback");
                }
            }
        };
        interceptor.setCallbackHandler(handler);
        interceptor.afterPropertiesSet();
        SaajSoapMessage message = loadSaajMessage("empty-soap.xml");
        interceptor.secureMessage(message, null);
        SOAPMessage result = message.getSaajMessage();
        assertNotNull("No result returned", result);
        assertXpathExists("BinarySecurityToken does not exist",
                "SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:BinarySecurityToken", result);
        assertXpathExists("Signature does not exist",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/xenc:EncryptedKey", result);
    }

    public void testDecrypt() throws Exception {
        interceptor.setPolicyConfiguration(new ClassPathResource("decrypt-config.xml", getClass()));
        CallbackHandler handler = new AbstractCallbackHandler() {

            @Override
            protected void handleInternal(Callback callback) {
                if (callback instanceof DecryptionKeyCallback) {
                    DecryptionKeyCallback keyCallback = (DecryptionKeyCallback) callback;
                    if (keyCallback.getRequest() instanceof DecryptionKeyCallback.X509CertificateBasedRequest) {
                        DecryptionKeyCallback.X509CertificateBasedRequest request =
                                (DecryptionKeyCallback.X509CertificateBasedRequest) keyCallback.getRequest();
                        assertEquals("Invalid certificate", certificate, request.getX509Certificate());
                        request.setPrivateKey(privateKey);
                    }
                    else {
                        fail("Unexpected request");
                    }
                }
                else {
                    fail("Unexpected callback");
                }
            }
        };
        interceptor.setCallbackHandler(handler);
        interceptor.afterPropertiesSet();
        SaajSoapMessage message = loadSaajMessage("encrypted-soap.xml");
        interceptor.validateMessage(message, null);
        SOAPMessage result = message.getSaajMessage();
        assertNotNull("No result returned", result);
        assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security", result);
    }

}
