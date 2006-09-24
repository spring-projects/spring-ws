/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.webflow;

import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A utility class for working with attribute and parameter collections used by Spring Web FLow.
 * @author Keith Donald
 */
public class CollectionUtils {

	/**
	 * The shared, singleton empty unmodifiable map instance.
	 */
	public static final UnmodifiableAttributeMap EMPTY_ATTRIBUTE_MAP = new UnmodifiableAttributeMap(
			Collections.EMPTY_MAP);

	/**
	 * The shared, singleton empty unmodifiable parameter map instance.
	 */
	public static final ParameterMap EMPTY_PARAMETER_MAP = new ParameterMap(Collections.EMPTY_MAP);

	/**
	 * The shared, singleton empty iterator instance.
	 */
	public static final EmptyIterator EMPTY_ITERATOR = new EmptyIterator();

	private CollectionUtils() {

	}

	private static class EmptyIterator implements Iterator, Serializable {
		private EmptyIterator() {

		}

		public boolean hasNext() {
			return false;
		}

		public Object next() {
			throw new UnsupportedOperationException("There are no elements");
		}

		public void remove() {
			throw new UnsupportedOperationException("There are no elements");
		}
	}

	private static class EnumerationIterator implements Iterator {

		private Enumeration enumeration;

		public EnumerationIterator(Enumeration enumeration) {
			this.enumeration = enumeration;
		}

		public boolean hasNext() {
			return enumeration.hasMoreElements();
		}

		public Object next() {
			return enumeration.nextElement();
		}

		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Not supported");
		}

	}

	/**
	 * Factory method that adapts an enumeration to an iterator.
	 * @param enumeration the enumeration
	 * @return the iterator.
	 */
	public static Iterator iterator(Enumeration enumeration) {
		return new EnumerationIterator(enumeration);
	}

	/**
	 * Factory method that returns a unmodifiable attribute map with a single entry.
	 * @param attributeName the attribute name
	 * @param attributeValue the attribute value
	 * @return
	 */
	public static UnmodifiableAttributeMap singleEntryMap(String attributeName, Object attributeValue) {
		Map map = new HashMap(1, 1);
		map.put(attributeName, attributeValue);
		return new UnmodifiableAttributeMap(map);
	}
}