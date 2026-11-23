/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

public class MockTransportOutputStream extends TransportOutputStream {

	private Map<String, String> headers = new HashMap<>();

	private OutputStream outputStream;

	public MockTransportOutputStream(OutputStream outputStream) {

		Assert.notNull(outputStream, "outputStream must not be null");
		this.outputStream = outputStream;
	}

	@Override
	protected OutputStream createOutputStream() {
		return this.outputStream;
	}

	public Map<String, String> getHeaders() {
		return this.headers;
	}

	@Override
	public void addHeader(String name, String value) {
		this.headers.put(name, value);
	}

}
