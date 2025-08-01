/*
 * Copyright 2005-present the original author or authors.
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
