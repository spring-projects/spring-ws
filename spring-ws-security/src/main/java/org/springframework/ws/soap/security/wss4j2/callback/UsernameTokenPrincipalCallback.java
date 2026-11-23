/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.security.wss4j2.callback;

import java.io.Serial;
import java.io.Serializable;

import javax.security.auth.callback.Callback;

import org.apache.wss4j.common.principal.WSUsernameTokenPrincipalImpl;

/**
 * Underlying security services instantiate and pass a
 * {@code UsernameTokenPrincipalCallback} to the {@code handle} method of a
 * {@code CallbackHandler} to pass a security {@code WSUsernameTokenPrincipal}.
 *
 * @author Arjen Poutsma
 * @author Jamin Hitchcock
 * @since 2.3.0
 * @see WSUsernameTokenPrincipalImpl
 */
public class UsernameTokenPrincipalCallback implements Callback, Serializable {

	@Serial
	private static final long serialVersionUID = -3022202225157082715L;

	private final WSUsernameTokenPrincipalImpl principal;

	/** Construct a {@code UsernameTokenPrincipalCallback}. */
	public UsernameTokenPrincipalCallback(WSUsernameTokenPrincipalImpl principal) {
		this.principal = principal;
	}

	/** Get the retrieved {@code Principal}. */
	public WSUsernameTokenPrincipalImpl getPrincipal() {
		return this.principal;
	}

}
