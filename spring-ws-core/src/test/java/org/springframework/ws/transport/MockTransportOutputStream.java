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

package org.springframework.ws.transport;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

public class MockTransportOutputStream extends TransportOutputStream {

	private Map<String, String> headers = new HashMap<String, String>();

	private OutputStream outputStream;

	public MockTransportOutputStream(OutputStream outputStream) {
		Assert.notNull(outputStream, "outputStream must not be null");
		this.outputStream = outputStream;
	}

	@Override
	protected OutputStream createOutputStream() throws IOException {
		return outputStream;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	@Override
	public void addHeader(String name, String value) throws IOException {
		headers.put(name, value);
	}
}
