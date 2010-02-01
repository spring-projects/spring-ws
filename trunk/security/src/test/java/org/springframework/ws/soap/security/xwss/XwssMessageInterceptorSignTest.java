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

import java.security.cert.X509Certificate;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.soap.SOAPMessage;

import com.sun.xml.wss.impl.callback.CertificateValidationCallback;
import com.sun.xml.wss.impl.callback.SignatureKeyCallback;

import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.security.callback.AbstractCallbackHandler;

public class XwssMessageInterceptorSignTest extends AbstractXwssMessageInterceptorKeyStoreTestCase {

    public void testSignDefaultCertificate() throws Exception {
        interceptor.setPolicyConfiguration(new ClassPathResource("sign-config.xml", getClass()));
        CallbackHandler handler = new AbstractCallbackHandler() {

            @Override
            protected void handleInternal(Callback callback) {
                if (callback instanceof SignatureKeyCallback) {
                    SignatureKeyCallback keyCallback = (SignatureKeyCallback) callback;
                    if (keyCallback.getRequest() instanceof SignatureKeyCallback.DefaultPrivKeyCertRequest) {
                        SignatureKeyCallback.DefaultPrivKeyCertRequest request =
                                (SignatureKeyCallback.DefaultPrivKeyCertRequest) keyCallback.getRequest();
                        request.setX509Certificate(certificate);
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
        SaajSoapMessage message = loadSaajMessage("empty-soap.xml");
        interceptor.secureMessage(message, null);
        SOAPMessage result = message.getSaajMessage();
        assertNotNull("No result returned", result);
        assertXpathExists("BinarySecurityToken does not exist",
                "SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:BinarySecurityToken", result);
        assertXpathExists("Signature does not exist", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/ds:Signature",
                result);
    }

    public void testSignAlias() throws Exception {
        interceptor.setPolicyConfiguration(new ClassPathResource("sign-alias-config.xml", getClass()));
        CallbackHandler handler = new AbstractCallbackHandler() {

            @Override
            protected void handleInternal(Callback callback) {
                if (callback instanceof SignatureKeyCallback) {
                    SignatureKeyCallback keyCallback = (SignatureKeyCallback) callback;
                    if (keyCallback.getRequest() instanceof SignatureKeyCallback.AliasPrivKeyCertRequest) {
                        SignatureKeyCallback.AliasPrivKeyCertRequest request =
                                (SignatureKeyCallback.AliasPrivKeyCertRequest) keyCallback.getRequest();
                        assertEquals("Invalid alias", "alias", request.getAlias());
                        request.setX509Certificate(certificate);
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
        SaajSoapMessage message = loadSaajMessage("empty-soap.xml");
        interceptor.secureMessage(message, null);
        SOAPMessage result = message.getSaajMessage();
        assertNotNull("No result returned", result);
        assertXpathExists("BinarySecurityToken does not exist",
                "SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:BinarySecurityToken", result);
        assertXpathExists("Signature does not exist", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/ds:Signature",
                result);
    }

    public void testValidateCertificate() throws Exception {
        interceptor.setPolicyConfiguration(new ClassPathResource("requireSignature-config.xml", getClass()));
        CallbackHandler handler = new AbstractCallbackHandler() {

            @Override
            protected void handleInternal(Callback callback) {
                if (callback instanceof CertificateValidationCallback) {
                    CertificateValidationCallback validationCallback = (CertificateValidationCallback) callback;
                    validationCallback.setValidator(new CertificateValidationCallback.CertificateValidator() {
                        public boolean validate(X509Certificate passedCertificate) {
                            assertEquals("Invalid certificate", certificate, passedCertificate);
                            return true;
                        }
                    });
                }
                else {
                    fail("Unexpected callback");
                }
            }
        };
        interceptor.setCallbackHandler(handler);
        interceptor.afterPropertiesSet();
        SaajSoapMessage message = loadSaajMessage("signed-soap.xml");
        interceptor.validateMessage(message, null);
        SOAPMessage result = message.getSaajMessage();
        assertNotNull("No result returned", result);
        assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security", result);
    }

}
