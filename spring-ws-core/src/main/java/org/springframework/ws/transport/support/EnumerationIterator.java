/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.support;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Adapts an {@link Enumeration} to follow the interface of {@link Iterator}.
 *
 * @param <T> the element type
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class EnumerationIterator<T> implements Iterator<T> {

	private final Enumeration<T> enumeration;

	public EnumerationIterator(Enumeration<T> enumeration) {
		this.enumeration = enumeration;
	}

	@Override
	public boolean hasNext() {
		return this.enumeration.hasMoreElements();
	}

	@Override
	public T next() {
		return this.enumeration.nextElement();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
