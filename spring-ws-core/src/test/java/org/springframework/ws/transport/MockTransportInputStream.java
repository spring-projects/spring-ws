/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport;

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
		this.headers = new HashMap<>();
	}

	@Override
	protected InputStream createInputStream() {
		return this.inputStream;
	}

	@Override
	public Iterator<String> getHeaderNames() {
		return this.headers.keySet().iterator();
	}

	@Override
	public Iterator<String> getHeaders(String name) {

		String[] values = StringUtils.delimitedListToStringArray(this.headers.get(name), ", ");
		return Arrays.asList(values).iterator();
	}

}
