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

package org.springframework.ws.soap.addressing.server;

import org.springframework.ws.soap.addressing.version.Addressing200408;
import org.springframework.ws.soap.addressing.version.AddressingVersion;

public class AddressingInterceptor200408Test extends AbstractAddressingInterceptorTestCase {

	@Override
	protected AddressingVersion getVersion() {
		return new Addressing200408();
	}

	@Override
	protected String getTestPath() {
		return "200408";
	}

	@Override
	public void testNoneReplyTo() throws Exception {
		// This version of the spec does not have none addresses
	}
}
