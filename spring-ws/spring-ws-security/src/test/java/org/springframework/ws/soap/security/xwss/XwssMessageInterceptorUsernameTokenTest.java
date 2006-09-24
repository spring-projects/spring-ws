/*
 * Copyright 2006 the original author or authors.
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

import com.sun.xml.wss.impl.callback.PasswordCallback;
import com.sun.xml.wss.impl.callback.PasswordValidationCallback;
import com.sun.xml.wss.impl.callback.TimestampValidationCallback;
import com.sun.xml.wss.impl.callback.UsernameCallback;

import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.soap.security.xwss.callback.AbstractCallbackHandler;

public class XwssMessageInterceptorUsernameTokenTest extends XwssMessageInterceptorTestCase {

    public void testAddUsernameTokenDigest() throws Exception {
        interceptor.setPolicyConfiguration(new ClassPathResource("usernameToken-digest-config.xml", getClass()));
        CallbackHandler handler = new AbstractCallbackHandler() {

            protected void handleInternal(Callback callback) {
                if (callback instanceof UsernameCallback) {
                    ((UsernameCallback) callback).setUsername("Bert");
                }
                else if (callback instanceof PasswordCallback) {
                    PasswordCallback passwordCallback = (PasswordCallback) callback;
                    passwordCallback.setPassword("Ernie");
                }
                else {
                    fail("Unexpected callback");
                }
            }
        };
        interceptor.setCallbackHandler(handler);
        interceptor.afterPropertiesSet();
        SOAPMessage message = loadSaajMessage("empty-soap.xml");
        SOAPMessage result = interceptor.secureMessage(message);
        assertNotNull("No result returned", result);
        assertXpathEvaluatesTo("Invalid Username", "Bert",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Username/text()", result);
        assertXpathExists("Password does not exist",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Password[@Type='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest']",
                result);
    }

    public void testAddUsernameTokenPlainText() throws Exception {
        interceptor.setPolicyConfiguration(new ClassPathResource("usernameToken-plainText-config.xml", getClass()));
        CallbackHandler handler = new AbstractCallbackHandler() {

            protected void handleInternal(Callback callback) {
                if (callback instanceof UsernameCallback) {
                    ((UsernameCallback) callback).setUsername("Bert");
                }
                else if (callback instanceof PasswordCallback) {
                    PasswordCallback passwordCallback = (PasswordCallback) callback;
                    passwordCallback.setPassword("Ernie");
                }
                else {
                    fail("Unexpected callback");
                }
            }
        };
        interceptor.setCallbackHandler(handler);
        interceptor.afterPropertiesSet();
        SOAPMessage message = loadSaajMessage("empty-soap.xml");
        SOAPMessage result = interceptor.secureMessage(message);
        assertNotNull("No result returned", result);
        assertXpathEvaluatesTo("Invalid Username", "Bert",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Username/text()", result);
        assertXpathEvaluatesTo("Invalid Password", "Ernie",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Password[@Type='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText']/text()",
                result);
    }

    public void testValidateUsernameTokenPlainText() throws Exception {
        interceptor
                .setPolicyConfiguration(new ClassPathResource("requireUsernameToken-plainText-config.xml", getClass()));
        CallbackHandler handler = new AbstractCallbackHandler() {

            protected void handleInternal(Callback callback) {
                if (callback instanceof PasswordValidationCallback) {
                    PasswordValidationCallback validationCallback = (PasswordValidationCallback) callback;
                    validationCallback.setValidator(new PasswordValidationCallback.PasswordValidator() {
                        public boolean validate(PasswordValidationCallback.Request request) {
                            if (request instanceof PasswordValidationCallback.PlainTextPasswordRequest) {
                                PasswordValidationCallback.PlainTextPasswordRequest passwordRequest =
                                        (PasswordValidationCallback.PlainTextPasswordRequest) request;
                                assertEquals("Invalid username", "Bert", passwordRequest.getUsername());
                                assertEquals("Invalid password", "Ernie", passwordRequest.getPassword());
                                return true;
                            }
                            else {
                                fail("Unexpected request");
                                return false;
                            }
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
        SOAPMessage message = loadSaajMessage("userNameTokenPlainText-soap.xml");
        SOAPMessage result = interceptor.validateMessage(message);
        assertNotNull("No result returned", result);
        assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security", result);
    }

    public void testValidateUsernameTokenDigest() throws Exception {
        interceptor.setPolicyConfiguration(new ClassPathResource("requireUsernameToken-digest-config.xml", getClass()));
        CallbackHandler handler = new AbstractCallbackHandler() {

            protected void handleInternal(Callback callback) {
                if (callback instanceof PasswordValidationCallback) {
                    PasswordValidationCallback validationCallback = (PasswordValidationCallback) callback;
                    if (validationCallback.getRequest() instanceof PasswordValidationCallback.DigestPasswordRequest) {
                        PasswordValidationCallback.DigestPasswordRequest passwordRequest =
                                (PasswordValidationCallback.DigestPasswordRequest) validationCallback.getRequest();
                        assertEquals("Invalid username", "Bert", passwordRequest.getUsername());
                        passwordRequest.setPassword("Ernie");
                        validationCallback.setValidator(new PasswordValidationCallback.DigestPasswordValidator());
                    }
                    else {
                        fail("Unexpected request");
                    }
                }
                else if (callback instanceof TimestampValidationCallback) {
                    TimestampValidationCallback validationCallback = (TimestampValidationCallback) callback;
                    validationCallback.setValidator(new TimestampValidationCallback.TimestampValidator() {
                        public void validate(TimestampValidationCallback.Request request) {
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
        SOAPMessage message = loadSaajMessage("userNameTokenDigest-soap.xml");
        SOAPMessage result = interceptor.validateMessage(message);
        assertNotNull("No result returned", result);
        assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security", result);
    }

}