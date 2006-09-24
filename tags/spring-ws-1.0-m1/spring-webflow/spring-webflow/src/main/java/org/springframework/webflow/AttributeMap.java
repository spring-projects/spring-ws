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

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * A generic, mutable attribute map with string keys.
 * 
 * @author Keith Donald
 */
public class AttributeMap extends AbstractAttributeMap {

	/**
	 * Creates a new attribute map, initially empty.
	 */
	public AttributeMap() {
		initAttributes(createTargetMap());
	}

	/**
	 * Creates a new attribute map wrapping the specified map.
	 */
	public AttributeMap(Map map) {
		Assert.notNull(map, "The target map is required");
		initAttributes(map);
	}

	/**
	 * Factory method that returns the target map storing the data in this
	 * attribute map.
	 * @return the target map
	 */
	protected Map createTargetMap() {
		return new HashMap();
	}

	public boolean equals(Object o) {
		if (!(o instanceof AttributeMap)) {
			return false;
		}
		AttributeMap other = (AttributeMap)o;
		return getMapInternal().equals(other.getMapInternal());
	}
	
	public int hashCode() {
		return getMapInternal().hashCode();
	}

	public UnmodifiableAttributeMap unmodifiable() {
		return new UnmodifiableAttributeMap(getMap());
	}

	public AttributeCollection union(AttributeCollection attributes) {
		if (attributes == null) {
			return new AttributeMap(getMapInternal());
		}
		else {
			Map map = createTargetMap();
			map.putAll(getMapInternal());
			map.putAll(attributes.getMap());
			return new AttributeMap(map);
		}
	}

	/**
	 * Put the attribute into this map.
	 * @param attributeName the attribute name.
	 * @param attributeValue the attribute value.
	 * @return the previous value of the attribute, or null of there was no
	 * previous value.
	 */
	public Object put(String attributeName, Object attributeValue) {
		return getMapInternal().put(attributeName, attributeValue);
	}

	/**
	 * Put all the attributes into this map.
	 * @param attributes the attributes to put into this scope.
	 * @return this, to support call chaining.
	 */
	public AttributeMap putAll(AttributeCollection attributes) {
		if (attributes == null) {
			return this;
		}
		getMapInternal().putAll(attributes.getMap());
		return this;
	}

	/**
	 * Remove an attribute from this map.
	 * @param attributeName the name of the attribute to remove
	 * @return previous value associated with specified attribute name, or
	 * <tt>null</tt> if there was no mapping for the name
	 */
	public Object remove(String attributeName) {
		return getMapInternal().remove(attributeName);
	}

	/**
	 * Clear the attributes in this map.
	 * @throws UnsupportedOperationException clear is not supported
	 * @return this, to support call chaining
	 */
	public AttributeMap clear() throws UnsupportedOperationException {
		getMapInternal().clear();
		return this;
	}

	/**
	 * Replace the contents of this attribute map with the contents of the
	 * provided collection.
	 * @param attributes the attribute collection
	 * @return this, to support call chaining
	 */
	public AttributeMap replaceWith(AttributeCollection attributes) throws UnsupportedOperationException {
		clear();
		putAll(attributes);
		return this;
	}
}