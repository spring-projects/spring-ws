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

package org.springframework.ws.soap.security.xwss.callback.acegi;

import com.sun.xml.wss.impl.callback.PasswordValidationCallback;
import junit.framework.TestCase;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.providers.TestingAuthenticationToken;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.easymock.MockControl;

public class AcegiPlainTextPasswordValidationCallbackHandlerTest extends TestCase {

    private AcegiPlainTextPasswordValidationCallbackHandler callbackHandler;

    private MockControl control;

    private AuthenticationManager mock;

    private PasswordValidationCallback callback;

    private String username;

    private String password;

    protected void setUp() throws Exception {
        callbackHandler = new AcegiPlainTextPasswordValidationCallbackHandler();
        control = MockControl.createControl(AuthenticationManager.class);
        mock = (AuthenticationManager) control.getMock();
        callbackHandler.setAuthenticationManager(mock);
        username = "Bert";
        password = "Ernie";
        PasswordValidationCallback.PlainTextPasswordRequest request =
                new PasswordValidationCallback.PlainTextPasswordRequest(username, password);
        callback = new PasswordValidationCallback(request);
    }

    public void testAuthenticateUserPlainTextValid() throws Exception {
        Authentication authResult = new TestingAuthenticationToken(username, password, new GrantedAuthority[0]);
        control.expectAndReturn(mock.authenticate(new UsernamePasswordAuthenticationToken(username, password)),
                authResult);
        control.replay();
        callbackHandler.handleInternal(callback);
        boolean authenticated = callback.getResult();
        assertTrue("Not authenticated", authenticated);
        control.verify();
    }

    public void testAuthenticateUserPlainTextInvalid() throws Exception {
        control.expectAndThrow(mock.authenticate(new UsernamePasswordAuthenticationToken(username, password)),
                new BadCredentialsException(""));
        control.replay();
        callbackHandler.handleInternal(callback);
        boolean authenticated = callback.getResult();
        assertFalse("Authenticated", authenticated);
        control.verify();
    }

}