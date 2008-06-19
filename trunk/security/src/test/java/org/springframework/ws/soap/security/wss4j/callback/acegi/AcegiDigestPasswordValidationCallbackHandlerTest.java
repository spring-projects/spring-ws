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

package org.springframework.ws.soap.security.wss4j.callback.acegi;

import junit.framework.TestCase;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.easymock.MockControl;

import org.springframework.ws.soap.security.wss4j.callback.UsernameTokenPrincipalCallback;

/** @author tareq */
public class AcegiDigestPasswordValidationCallbackHandlerTest extends TestCase {

    private AcegiDigestPasswordValidationCallbackHandler callbackHandler;

    private GrantedAuthorityImpl grantedAuthority;

    private UserDetailsService userDetailsService;

    private MockControl control;

    private WSUsernameTokenPrincipal principal;

    private UsernameTokenPrincipalCallback callback;

    private UserDetails user;

    protected void setUp() throws Exception {
        callbackHandler = new AcegiDigestPasswordValidationCallbackHandler();

        grantedAuthority = new GrantedAuthorityImpl("ROLE_1");
        user = new User("Ernie", "Bert", true, true, true, true, new GrantedAuthority[]{grantedAuthority});

        control = MockControl.createControl(UserDetailsService.class);
        userDetailsService = (UserDetailsService) control.getMock();
        userDetailsService.loadUserByUsername("Ernie");
        control.setDefaultReturnValue(user);
        control.replay();
        callbackHandler.setUserDetailsService(userDetailsService);

        principal = new WSUsernameTokenPrincipal("Ernie", true);
        callback = new UsernameTokenPrincipalCallback(principal);

    }

    public void testHandleUsernameTokenPrincipal() throws Exception {
        callbackHandler.handleUsernameTokenPrincipal(callback);
        SecurityContext context = SecurityContextHolder.getContext();
        assertNotNull("SecurityContext must not be null", context);
        Authentication authentication = context.getAuthentication();
        assertNotNull("Authentication must not be null", authentication);
        GrantedAuthority[] authorities = authentication.getAuthorities();
        assertTrue("GrantedAuthority[] must not be null or empty", (authorities != null && authorities.length > 0));
        assertEquals("Unexpected authority", grantedAuthority, authorities[0]);
    }
}
