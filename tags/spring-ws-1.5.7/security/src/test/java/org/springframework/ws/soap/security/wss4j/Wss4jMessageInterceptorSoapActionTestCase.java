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

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.wss4j.callback.SimplePasswordValidationCallbackHandler;

public abstract class Wss4jMessageInterceptorSoapActionTestCase extends Wss4jTestCase {

    private static final String SOAP_ACTION = "\"http://test\"";

    private Properties users = new Properties();

    private Wss4jSecurityInterceptor interceptor;

    protected void onSetup() throws Exception {
        users.setProperty("Bert", "Ernie");
        interceptor = new Wss4jSecurityInterceptor();
        interceptor.setValidationActions("UsernameToken");
        interceptor.setSecurementActions("UsernameToken");
        interceptor.setSecurementPasswordType(WSConstants.PW_TEXT);
        SimplePasswordValidationCallbackHandler callbackHandler = new SimplePasswordValidationCallbackHandler();
        callbackHandler.setUsers(users);
        interceptor.setValidationCallbackHandler(callbackHandler);

        interceptor.afterPropertiesSet();
    }

    public void testPreserveSoapActionOnValidation() throws Exception {
        SoapMessage message = loadMessage("usernameTokenPlainText-soap.xml");
        message.setSoapAction(SOAP_ACTION);
        MessageContext messageContext = new DefaultMessageContext(message, getMessageFactory());
        interceptor.validateMessage(message, messageContext);

        assertNotNull("Soap Action must not be null", message.getSoapAction());
        assertEquals("Soap Action is different from expected", SOAP_ACTION, message.getSoapAction());
    }

    public void testPreserveSoapActionOnSecurement() throws Exception {
        SoapMessage message = loadMessage("empty-soap.xml");
        message.setSoapAction(SOAP_ACTION);
        interceptor.setSecurementUsername("Bert");
        interceptor.setSecurementPassword("Ernie");
        MessageContext messageContext = getMessageContext(message);
        interceptor.secureMessage(message, messageContext);

        assertNotNull("Soap Action must not be null", message.getSoapAction());
        assertEquals("Soap Action is different from expected", SOAP_ACTION, message.getSoapAction());

    }
}
