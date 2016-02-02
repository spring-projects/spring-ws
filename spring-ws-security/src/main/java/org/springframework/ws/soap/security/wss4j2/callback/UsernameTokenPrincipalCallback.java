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

package org.springframework.ws.soap.security.wss4j2.callback;

import java.io.Serializable;
import javax.security.auth.callback.Callback;

import org.apache.wss4j.common.principal.WSUsernameTokenPrincipalImpl;

/**
 * Underlying security services instantiate and pass a {@code UsernameTokenPrincipalCallback} to the
 * {@code handle} method of a {@code CallbackHandler} to pass a security
 * {@code WSUsernameTokenPrincipal}.
 *
 * @author Arjen Poutsma
 * @author Jamin Hitchcock
 * @see WSUsernameTokenPrincipalImpl
 * @since 2.3.0
 */
public class UsernameTokenPrincipalCallback implements Callback, Serializable {

	private static final long serialVersionUID = -3022202225157082715L;

	private final WSUsernameTokenPrincipalImpl principal;

	/** Construct a {@code UsernameTokenPrincipalCallback}. */
	public UsernameTokenPrincipalCallback(WSUsernameTokenPrincipalImpl principal) {
		this.principal = principal;
	}

	/** Get the retrieved {@code Principal}. */
	public WSUsernameTokenPrincipalImpl getPrincipal() {
		return principal;
	}
}
