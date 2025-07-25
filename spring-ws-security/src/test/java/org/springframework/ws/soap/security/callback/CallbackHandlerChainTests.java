/*
 * Copyright 2005-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
