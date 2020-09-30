/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.soap.security.callback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.junit.Test;

public class CallbackHandlerChainTest {

	private CallbackHandler supported = new CallbackHandler() {
		public void handle(Callback[] callbacks) {}
	};

	private CallbackHandler unsupported = new CallbackHandler() {
		public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
			throw new UnsupportedCallbackException(callbacks[0]);
		}
	};

	private Callback callback = new Callback() {};

	@Test
	public void testSupported() throws Exception {
		CallbackHandlerChain chain = new CallbackHandlerChain(new CallbackHandler[] { supported });
		chain.handle(new Callback[] { callback });
	}

	@Test
	public void testUnsupportedSupported() throws Exception {
		CallbackHandlerChain chain = new CallbackHandlerChain(new CallbackHandler[] { unsupported, supported });
		chain.handle(new Callback[] { callback });
	}

	@Test(expected = UnsupportedCallbackException.class)
	public void testUnsupported() throws Exception {
		CallbackHandlerChain chain = new CallbackHandlerChain(new CallbackHandler[] { unsupported });
		chain.handle(new Callback[] { callback });
	}
}
