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
import org.easymock.MockControl;

import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationManager;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.TestingAuthenticationToken;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.memory.InMemoryDaoImpl;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.wss4j.callback.SpringDigestPasswordValidationCallbackHandler;
import org.springframework.ws.soap.security.wss4j.callback.SpringPlainTextPasswordValidationCallbackHandler;

public abstract class Wss4jMessageInterceptorSpringSecurityCallbackHandlerTestCase extends Wss4jTestCase {

    private Properties users = new Properties();

    private MockControl control;

    private AuthenticationManager mock;

    protected void onSetup() throws Exception {
        control = MockControl.createControl(AuthenticationManager.class);
        mock = (AuthenticationManager) control.getMock();
        users.setProperty("Bert", "Ernie,ROLE_TEST");
    }

    protected void tearDown() throws Exception {
        control.verify();
        SecurityContextHolder.clearContext();
    }

    public void testValidateUsernameTokenPlainText() throws Exception {
        EndpointInterceptor interceptor = prepareInterceptor("UsernameToken", true, false);
        SoapMessage message = loadSoap11Message("usernameTokenPlainText-soap.xml");
        MessageContext messageContext = new DefaultMessageContext(message, getSoap11MessageFactory());
        interceptor.handleRequest(messageContext, null);
        assertValidateUsernameToken(message);

        // test clean up
        messageContext.getResponse();
        interceptor.handleResponse(messageContext, null);
        assertNull("Authentication created", SecurityContextHolder.getContext().getAuthentication());
    }

    public void testValidateUsernameTokenDigest() throws Exception {
        EndpointInterceptor interceptor = prepareInterceptor("UsernameToken", true, true);
        SoapMessage message = loadSoap11Message("usernameTokenDigest-soap.xml");
        MessageContext messageContext = new DefaultMessageContext(message, getSoap11MessageFactory());
        interceptor.handleRequest(messageContext, null);
        assertValidateUsernameToken(message);

        // test clean up
        messageContext.getResponse();
        interceptor.handleResponse(messageContext, null);
        assertNull("Authentication created", SecurityContextHolder.getContext().getAuthentication());
    }

    protected void assertValidateUsernameToken(SoapMessage message) throws Exception {
        Object result = getMessage(message);
        assertNotNull("No result returned", result);
        assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security",
                getDocument(message));
        assertNotNull("No Authentication created", SecurityContextHolder.getContext().getAuthentication());
    }

    protected EndpointInterceptor prepareInterceptor(String actions, boolean validating, boolean digest)
            throws Exception {
        Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
        if (validating) {
            interceptor.setValidationActions(actions);
        }
        else {
            interceptor.setSecurementActions(actions);
        }
        if (digest) {
            SpringDigestPasswordValidationCallbackHandler callbackHandler =
                    new SpringDigestPasswordValidationCallbackHandler();
            InMemoryDaoImpl userDetailsService = new InMemoryDaoImpl();
            userDetailsService.setUserProperties(users);
            userDetailsService.afterPropertiesSet();
            callbackHandler.setUserDetailsService(userDetailsService);
            interceptor.setSecurementPasswordType(WSConstants.PW_DIGEST);
            interceptor.setValidationCallbackHandler(callbackHandler);
            interceptor.afterPropertiesSet();
        }
        else {
            SpringPlainTextPasswordValidationCallbackHandler callbackHandler =
                    new SpringPlainTextPasswordValidationCallbackHandler();
            Authentication authResult = new TestingAuthenticationToken("Bert", "Ernie", new GrantedAuthority[0]);
            control.expectAndReturn(mock.authenticate(new UsernamePasswordAuthenticationToken("Bert", "Ernie")),
                    authResult);
            callbackHandler.setAuthenticationManager(mock);
            callbackHandler.afterPropertiesSet();
            interceptor.setSecurementPasswordType(WSConstants.PW_TEXT);
            interceptor.setValidationCallbackHandler(callbackHandler);
            interceptor.afterPropertiesSet();
        }
        control.replay();
        return interceptor;
    }
}