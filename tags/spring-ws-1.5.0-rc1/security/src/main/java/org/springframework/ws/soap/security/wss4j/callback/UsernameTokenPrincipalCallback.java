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

package org.springframework.ws.soap.security.wss4j.callback;

import java.io.Serializable;
import javax.security.auth.callback.Callback;

import org.apache.ws.security.WSUsernameTokenPrincipal;

/**
 * Underlying security services instantiate and pass a <code>UsernameTokenPrincipalCallback</code> to the
 * <code>handle</code> method of a <code>CallbackHandler</code> to pass a security
 * <code>WSUsernameTokenPrincipal</code>.
 *
 * @author Arjen Poutsma
 * @see WSUsernameTokenPrincipal
 * @since 1.5.0
 */
public class UsernameTokenPrincipalCallback implements Callback, Serializable {

    private static final long serialVersionUID = -3022202225157082715L;

    private final WSUsernameTokenPrincipal principal;

    /** Construct a <code>UsernameTokenPrincipalCallback</code>. */
    public UsernameTokenPrincipalCallback(WSUsernameTokenPrincipal principal) {
        this.principal = principal;
    }

    /** Get the retrieved <code>Principal</code>. */
    public WSUsernameTokenPrincipal getPrincipal() {
        return principal;
    }
}
