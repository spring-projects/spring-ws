/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.transform;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.transform.stream.StreamSource;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * Convenient subclass of {@code StreamSource} that reads from a {@code StringReader}. The
 * string to be read can be set via the constructor.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class StringSource extends StreamSource {

	private final String content;

	/**
	 * Initializes a new instance of the {@code StringSource} with the given string
	 * content.
	 * @param content the content
	 */
	public StringSource(String content) {
		Assert.notNull(content, "'content' must not be null");
		this.content = content;
	}

	@Override
	public Reader getReader() {
		return new StringReader(this.content);
	}

	/**
	 * Throws {@link UnsupportedOperationException}.
	 * @throws UnsupportedOperationException always
	 */
	@Override
	public void setInputStream(InputStream inputStream) {
		throw new UnsupportedOperationException("setInputStream is not supported");
	}

	/**
	 * Returns {@code null}.
	 * @return {@code null}
	 */
	@Override
	public @Nullable InputStream getInputStream() {
		return null;
	}

	/**
	 * Throws {@link UnsupportedOperationException}.
	 * @throws UnsupportedOperationException always
	 */
	@Override
	public void setReader(Reader reader) {
		throw new UnsupportedOperationException("setReader is not supported");
	}

	@Override
	public String toString() {
		return this.content;
	}

}
