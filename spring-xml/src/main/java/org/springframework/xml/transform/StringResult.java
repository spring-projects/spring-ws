/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.transform;

import java.io.StringWriter;

import javax.xml.transform.stream.StreamResult;

/**
 * Convenient subclass of {@code StreamResult} that writes to a {@code StringWriter}. The
 * resulting string can be retrieved via {@code toString()}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see #toString()
 */
public class StringResult extends StreamResult {

	public StringResult() {
		super(new StringWriter());
	}

	/** Returns the written XML as a string. */
	public String toString() {
		return getWriter().toString();
	}

}
