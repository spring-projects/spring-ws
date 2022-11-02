/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ws.soap.security.xwss.callback;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.sun.xml.wss.impl.callback.PasswordCallback;
import com.sun.xml.wss.impl.callback.UsernameCallback;

public class SpringUsernamePasswordCallbackHandlerTest {

	private SpringUsernamePasswordCallbackHandler handler;

	@BeforeEach
	public void setUp() {

		handler = new SpringUsernamePasswordCallbackHandler();
		Authentication authentication = new UsernamePasswordAuthenticationToken("Bert", "Ernie");
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@AfterEach
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void testUsernameCallback() throws Exception {

		UsernameCallback usernameCallback = new UsernameCallback();
		handler.handleInternal(usernameCallback);

		assertThat(usernameCallback.getUsername()).isEqualTo("Bert");
	}

	@Test
	public void testPasswordCallback() throws Exception {

		PasswordCallback passwordCallback = new PasswordCallback();
		handler.handleInternal(passwordCallback);

		assertThat(passwordCallback.getPassword()).isEqualTo("Ernie");
	}
}
