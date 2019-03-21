/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.soap.security.xwss.callback;

import org.junit.Before;
import org.junit.Test;

public class KeyStoreCallbackHandlerTest {

	private KeyStoreCallbackHandler handler;

	@Before
	public void setUp() throws Exception {
		handler = new KeyStoreCallbackHandler();
	}

	@Test
	public void testLoadDefaultTrustStore() throws Exception {
		System.setProperty("javax.net.ssl.trustStore",
				"/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Home/");
		handler.loadDefaultTrustStore();
	}
}