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

import com.sun.xml.wss.impl.callback.TimestampValidationCallback;
import org.junit.Before;
import org.junit.Test;

public class DefaultTimestampValidatorTest {

	private DefaultTimestampValidator validator;

	@Before
	public void setUp() throws Exception {
		validator = new DefaultTimestampValidator();
	}

	@Test
	public void testValidate() throws Exception {
		TimestampValidationCallback.Request request = new TimestampValidationCallback.UTCTimestampRequest(
				"2006-09-25T20:42:50Z", "2107-09-25T20:42:50Z", 100, Long.MAX_VALUE);
		validator.validate(request);
	}

	@Test
	public void testValidateNoExpired() throws Exception {
		TimestampValidationCallback.Request request =
				new TimestampValidationCallback.UTCTimestampRequest("2006-09-25T20:42:50Z", null, 100, Long.MAX_VALUE);
		validator.validate(request);
	}
}