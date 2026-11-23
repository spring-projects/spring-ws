/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.security.callback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CallbackHandlerChainTests {

	private CallbackHandler supported = callbacks -> {
	};

	private CallbackHandler unsupported = callbacks -> {
		throw new UnsupportedCallbackException(callbacks[0]);
	};

	private Callback callback = new Callback() {
	};

	@Test
	void testSupported() throws Exception {

		CallbackHandlerChain chain = new CallbackHandlerChain(new CallbackHandler[] { this.supported });
		chain.handle(new Callback[] { this.callback });
	}

	@Test
	void testUnsupportedSupported() throws Exception {

		CallbackHandlerChain chain = new CallbackHandlerChain(
				new CallbackHandler[] { this.unsupported, this.supported });
		chain.handle(new Callback[] { this.callback });
	}

	@Test
	void testUnsupported() {

		assertThatExceptionOfType(UnsupportedCallbackException.class).isThrownBy(() -> {

			CallbackHandlerChain chain = new CallbackHandlerChain(new CallbackHandler[] { this.unsupported });
			chain.handle(new Callback[] { this.callback });
		});
	}

}
