/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.security.wss4j.callback;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;

import org.springframework.ws.soap.security.callback.AbstractCallbackHandler;
import org.springframework.ws.soap.security.callback.CleanupCallback;

/**
 * Abstract base class for {@link javax.security.auth.callback.CallbackHandler} implementations that handle {@link
 * WSPasswordCallback} callbacks.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 * @deprecated Transition to {@link org.springframework.ws.soap.security.wss4j2.callback.AbstractWsPasswordCallbackHandler}
 */
@Deprecated
public abstract class AbstractWsPasswordCallbackHandler extends AbstractCallbackHandler {

	/**
	 * Handles {@link WSPasswordCallback} callbacks. Inspects the callback {@link WSPasswordCallback#getUsage() usage}
	 * code, and calls the various {@code handle*} template methods.
	 *
	 * @param callback the callback
	 * @throws IOException					in case of I/O errors
	 * @throws UnsupportedCallbackException when the callback is not supported
	 */
	@Override
	protected final void handleInternal(Callback callback) throws IOException, UnsupportedCallbackException {
		if (callback instanceof WSPasswordCallback) {
			WSPasswordCallback passwordCallback = (WSPasswordCallback) callback;
			switch (passwordCallback.getUsage()) {
				case WSPasswordCallback.DECRYPT:
					handleDecrypt(passwordCallback);
					break;
				case WSPasswordCallback.USERNAME_TOKEN:
					handleUsernameToken(passwordCallback);
					break;
				case WSPasswordCallback.SIGNATURE:
					handleSignature(passwordCallback);
					break;
				case WSPasswordCallback.SECURITY_CONTEXT_TOKEN:
					handleSecurityContextToken(passwordCallback);
					break;
				case WSPasswordCallback.CUSTOM_TOKEN:
					handleCustomToken(passwordCallback);
					break;
				case WSPasswordCallback.SECRET_KEY:
					handleSecretKey(passwordCallback);
					break;
				default:
					throw new UnsupportedCallbackException(callback,
							"Unknown usage [" + passwordCallback.getUsage() + "]");
			}
		}
		else if (callback instanceof CleanupCallback) {
			handleCleanup((CleanupCallback) callback);
		}
		else if (callback instanceof UsernameTokenPrincipalCallback) {
			handleUsernameTokenPrincipal((UsernameTokenPrincipalCallback) callback);
		}
		else {
			throw new UnsupportedCallbackException(callback);
		}
	}

	/**
	 * Invoked when the callback has a {@link WSPasswordCallback#DECRYPT} usage.
	 *
	 * <p>This method is invoked when WSS4J needs a password to get the private key of the {@link
	 * WSPasswordCallback#getIdentifier() identifier} (username) from the keystore. WSS4J uses this private key to
	 * decrypt the session (symmetric) key. Because the encryption method uses the public key to encrypt the session key
	 * it needs no password (a public key is usually not protected by a password).
	 *
	 * <p>Default implementation throws an {@link UnsupportedCallbackException}.
	 */
	protected void handleDecrypt(WSPasswordCallback callback) throws IOException, UnsupportedCallbackException {
		throw new UnsupportedCallbackException(callback);
	}

	/**
	 * Invoked when the callback has a {@link WSPasswordCallback#USERNAME_TOKEN} usage.
	 *
	 * <p>This method is invoked when WSS4J needs the password to fill in or to verify a UsernameToken.
	 *
	 * <p>Default implementation throws an {@link UnsupportedCallbackException}.
	 */
	protected void handleUsernameToken(WSPasswordCallback callback) throws IOException, UnsupportedCallbackException {
		throw new UnsupportedCallbackException(callback);
	}

	/**
	 * Invoked when the callback has a {@link WSPasswordCallback#SIGNATURE} usage.
	 *
	 * <p>This method is invoked when WSS4J needs the password to get the private key of the {@link
	 * WSPasswordCallback#getIdentifier() identifier} (username) from the keystore. WSS4J uses this private key to
	 * produce a signature. The signature verfication uses the public key to verfiy the signature.
	 *
	 * <p>Default implementation throws an {@link UnsupportedCallbackException}.
	 */
	protected void handleSignature(WSPasswordCallback callback) throws IOException, UnsupportedCallbackException {
		throw new UnsupportedCallbackException(callback);
	}

	/**
	 * Invoked when the callback has a {@link WSPasswordCallback#SECURITY_CONTEXT_TOKEN} usage.
	 *
	 * <p>This method is invoked when WSS4J needs the key to to be associated with a SecurityContextToken.
	 *
	 * <p>Default implementation throws an {@link UnsupportedCallbackException}.
	 */
	protected void handleSecurityContextToken(WSPasswordCallback callback)
			throws IOException, UnsupportedCallbackException {
		throw new UnsupportedCallbackException(callback);
	}

	/**
	 * Invoked when the callback has a {@link WSPasswordCallback#CUSTOM_TOKEN} usage.
	 *
	 * <p>Default implementation throws an {@link UnsupportedCallbackException}.
	 */
	protected void handleCustomToken(WSPasswordCallback callback) throws IOException, UnsupportedCallbackException {
		throw new UnsupportedCallbackException(callback);
	}

	/**
	 * Invoked when the callback has a {@link WSPasswordCallback#SECRET_KEY} usage.
	 *
	 * <p>Default implementation throws an {@link UnsupportedCallbackException}.
	 */
	protected void handleSecretKey(WSPasswordCallback callback) throws IOException, UnsupportedCallbackException {
		throw new UnsupportedCallbackException(callback);
	}

	/**
	 * Invoked when a {@link CleanupCallback} is passed to {@link #handle(Callback[])}.
	 *
	 * <p>Default implementation throws an {@link UnsupportedCallbackException}.
	 */
	protected void handleCleanup(CleanupCallback callback) throws IOException, UnsupportedCallbackException {
		throw new UnsupportedCallbackException(callback);
	}

	/**
	 * Invoked when a {@link UsernameTokenPrincipalCallback} is passed to {@link #handle(Callback[])}.
	 *
	 * <p>Default implementation throws an {@link UnsupportedCallbackException}.
	 */
	protected void handleUsernameTokenPrincipal(UsernameTokenPrincipalCallback callback)
			throws IOException, UnsupportedCallbackException {
		throw new UnsupportedCallbackException(callback);
	}
}
