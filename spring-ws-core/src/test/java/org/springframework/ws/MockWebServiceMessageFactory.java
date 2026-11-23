/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

public class MockWebServiceMessageFactory implements WebServiceMessageFactory {

	@Override
	public MockWebServiceMessage createWebServiceMessage() {
		return new MockWebServiceMessage();
	}

	@Override
	public MockWebServiceMessage createWebServiceMessage(InputStream inputStream) throws IOException {

		try {
			return new MockWebServiceMessage(new StreamSource(inputStream));
		}
		catch (TransformerException ex) {
			throw new IOException(ex.getMessage());
		}
	}

}
