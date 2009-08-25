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

import java.util.Properties;

import org.apache.ws.security.WSConstants;
import org.w3c.dom.Document;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.wss4j.callback.SimplePasswordValidationCallbackHandler;

public abstract class Wss4jMessageInterceptorUsernameTokenTestCase extends Wss4jTestCase {

    private Properties users = new Properties();

    protected void onSetup() throws Exception {
        users.setProperty("Bert", "Ernie");
    }

    public void testValidateUsernameTokenPlainText() throws Exception {
        Wss4jSecurityInterceptor interceptor = prepareInterceptor("UsernameToken", true, false);
        SoapMessage message = loadMessage("usernameTokenPlainText-soap.xml");
        MessageContext messageContext = new DefaultMessageContext(message, getMessageFactory());
        interceptor.validateMessage(message, messageContext);
        assertValidateUsernameToken(message);
    }

    public void testValidateUsernameTokenDigest() throws Exception {
        Wss4jSecurityInterceptor interceptor = prepareInterceptor("UsernameToken", true, true);
        SoapMessage message = loadMessage("usernameTokenDigest-soap.xml");
        MessageContext messageContext = new DefaultMessageContext(message, getMessageFactory());
        interceptor.validateMessage(message, messageContext);
        assertValidateUsernameToken(message);
    }

    public void testAddUsernameTokenPlainText() throws Exception {
        Wss4jSecurityInterceptor interceptor = prepareInterceptor("UsernameToken", false, false);
        interceptor.setSecurementUsername("Bert");
        interceptor.setSecurementPassword("Ernie");
        SoapMessage message = loadMessage("empty-soap.xml");

        MessageContext messageContext = getMessageContext(message);

        interceptor.secureMessage(message, messageContext);
        assertAddUsernameTokenPlainText(message);
    }

    public void testAddUsernameTokenDigest() throws Exception {
        Wss4jSecurityInterceptor interceptor = prepareInterceptor("UsernameToken", false, true);
        interceptor.setSecurementUsername("Bert");
        interceptor.setSecurementPassword("Ernie");
        SoapMessage message = loadMessage("empty-soap.xml");

        MessageContext messageContext = getMessageContext(message);
        interceptor.secureMessage(message, messageContext);
        assertAddUsernameTokenDigest(message);
    }

    protected void assertValidateUsernameToken(SoapMessage message) throws Exception {
        Object result = getMessage(message);
        assertNotNull("No result returned", result);
        assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security",
                getDocument(message));
    }

    protected void assertAddUsernameTokenPlainText(SoapMessage message) throws Exception {
        Object result = getMessage(message);
        assertNotNull("No result returned", result);
        Document doc = getDocument(message);
        assertXpathEvaluatesTo("Invalid Username", "Bert",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Username/text()", doc);
        assertXpathEvaluatesTo("Invalid Password", "Ernie",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Password[@Type='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText']/text()",
                doc);
    }

    protected void assertAddUsernameTokenDigest(SoapMessage message) throws Exception {
        Object result = getMessage(message);
        Document doc = getDocument(message);
        assertNotNull("No result returned", result);
        assertXpathEvaluatesTo("Invalid Username", "Bert",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Username/text()", doc);
        assertXpathExists("Password does not exist",
                "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Password[@Type='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest']",
                doc);

    }

    protected Wss4jSecurityInterceptor prepareInterceptor(String actions, boolean validating, boolean digest)
            throws Exception {
        Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
        if (validating) {
            interceptor.setValidationActions(actions);
        }
        else {
            interceptor.setSecurementActions(actions);
        }
        SimplePasswordValidationCallbackHandler callbackHandler = new SimplePasswordValidationCallbackHandler();
        callbackHandler.setUsers(users);
        if (digest) {
//            callbackHandler.setPasswordDigestRequired(true);
//            callbackHandler.setPasswordPlainTextRequired(false);
            interceptor.setSecurementPasswordType(WSConstants.PW_DIGEST);
        }
        else {
//            callbackHandler.setPasswordDigestRequired(false);
//            callbackHandler.setPasswordPlainTextRequired(true);
            interceptor.setSecurementPasswordType(WSConstants.PW_TEXT);
        }
        interceptor.setValidationCallbackHandler(callbackHandler);
        interceptor.afterPropertiesSet();
        return interceptor;
    }
}
