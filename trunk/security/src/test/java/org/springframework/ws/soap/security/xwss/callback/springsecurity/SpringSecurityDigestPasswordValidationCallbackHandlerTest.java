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

package org.springframework.ws.soap.security.xwss.callback.springsecurity;

import com.sun.xml.wss.impl.callback.PasswordValidationCallback;
import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.TestingAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.ws.soap.security.callback.CleanupCallback;

public class SpringSecurityDigestPasswordValidationCallbackHandlerTest extends TestCase {

    private SpringSecurityDigestPasswordValidationCallbackHandler callbackHandler;

    private MockControl control;

    private UserDetailsService mock;

    private String username;

    private String password;

    private PasswordValidationCallback callback;

    protected void setUp() throws Exception {
        callbackHandler = new SpringSecurityDigestPasswordValidationCallbackHandler();
        control = MockControl.createControl(UserDetailsService.class);
        mock = (UserDetailsService) control.getMock();
        callbackHandler.setUserDetailsService(mock);
        username = "Bert";
        password = "Ernie";
        String nonce = "9mdsYDCrjjYRur0rxzYt2oD7";
        String passwordDigest = "kwNstEaiFOrI7B31j7GuETYvdgk=";
        String creationTime = "2006-06-01T23:48:42Z";
        PasswordValidationCallback.DigestPasswordRequest request =
                new PasswordValidationCallback.DigestPasswordRequest(username, passwordDigest, nonce, creationTime);
        callback = new PasswordValidationCallback(request);
    }

    protected void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
    }

    public void testAuthenticateUserDigestUserNotFound() throws Exception {
        control.expectAndThrow(mock.loadUserByUsername(username), new UsernameNotFoundException(username));
        control.replay();
        callbackHandler.handleInternal(callback);
        boolean authenticated = callback.getResult();
        assertFalse("Authenticated", authenticated);
        assertNull("Authentication created", SecurityContextHolder.getContext().getAuthentication());
        control.verify();
    }

    public void testAuthenticateUserDigestValid() throws Exception {
        User user = new User(username, password, true, true, true, true, new GrantedAuthority[0]);
        control.expectAndReturn(mock.loadUserByUsername(username), user);
        control.replay();
        callbackHandler.handleInternal(callback);
        boolean authenticated = callback.getResult();
        assertTrue("Not authenticated", authenticated);
        assertNotNull("No Authentication created", SecurityContextHolder.getContext().getAuthentication());
        control.verify();
    }

    public void testAuthenticateUserDigestValidInvalid() throws Exception {
        User user = new User(username, "Big bird", true, true, true, true, new GrantedAuthority[0]);
        control.expectAndReturn(mock.loadUserByUsername(username), user);
        control.replay();
        callbackHandler.handleInternal(callback);
        boolean authenticated = callback.getResult();
        assertFalse("Authenticated", authenticated);
        assertNull("Authentication created", SecurityContextHolder.getContext().getAuthentication());
        control.verify();
    }

    public void testCleanUp() throws Exception {
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(new Object(), new Object(), new GrantedAuthority[0]);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        CleanupCallback cleanupCallback = new CleanupCallback();
        callbackHandler.handleInternal(cleanupCallback);
        assertNull("Authentication created", SecurityContextHolder.getContext().getAuthentication());
    }

}