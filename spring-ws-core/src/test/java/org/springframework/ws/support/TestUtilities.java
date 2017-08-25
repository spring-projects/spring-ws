/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.ws.support;

import org.springframework.util.ClassUtils;

/**
 * @author Greg Turnquist
 */
public final class TestUtilities {

	public static boolean SPRING5;

	static {
		ClassLoader classLoader = TestUtilities.class.getClassLoader();
		try {
			ClassUtils.forName("org.springframework.http.server.reactive.ReactorHttpHandlerAdapter", classLoader);
			SPRING5 = true;
		} catch (ClassNotFoundException e) {
			SPRING5 = false;
		}

	}
}
