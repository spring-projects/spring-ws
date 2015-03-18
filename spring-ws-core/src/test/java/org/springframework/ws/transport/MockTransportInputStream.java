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

package org.springframework.ws.transport;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class MockTransportInputStream extends TransportInputStream {

	private Map<String, String> headers;

	private InputStream inputStream;

	public MockTransportInputStream(InputStream inputStream, Map<String, String> headers) {
		Assert.notNull(inputStream, "inputStream must not be null");
		Assert.notNull(headers, "headers must not be null");
		this.inputStream = inputStream;
		this.headers = headers;
	}

	public MockTransportInputStream(InputStream inputStream) {
		Assert.notNull(inputStream, "inputStream must not be null");
		this.inputStream = inputStream;
		headers = new HashMap<String, String>();
	}

	@Override
	protected InputStream createInputStream() throws IOException {
		return inputStream;
	}

	@Override
	public Iterator<String> getHeaderNames() throws IOException {
		return headers.keySet().iterator();
	}

	@Override
	public Iterator<String> getHeaders(String name) throws IOException {
		String[] values = StringUtils.delimitedListToStringArray(headers.get(name), ", ");
		return Arrays.asList(values).iterator();
	}
}
