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

import org.springframework.binding.util.MapAdaptable;

/**
 * An interface for objects that manage the storage of attribute values.
 * 
 * @author Keith Donald
 */
public interface AttributeCollection extends MapAdaptable {

	/**
	 * Returns the number of attributes in this collection.
	 * @return the number of attributes in this collection
	 */
	public int size();

	/**
	 * Get an attribute value out of this collection, returning
	 * <code>null</code> if not found.
	 * @param attributeName the attribute name
	 * @return the attribute value
	 */
	public Object get(String attributeName);

	/**
	 * Returns this collection as an {@link UnmodifiableAttributeMap}, which
	 * provides access to the attributes in this collection in an immutable,
	 * more strongly-typed manner.
	 * @return the attribute collection as an unmodifiable map.
	 */
	public UnmodifiableAttributeMap unmodifiable();
		
	/**
	 * Merge the attributes in the provided collection this collection and
	 * return a copy containing the union.
	 * @param attributes the attributes to merge in; if null, this should be returned.
	 * @return a new attribute collection, the union of this collection and the one provided; 
	 * or this, if a null attributes argument was provided.
	 */
	public AttributeCollection union(AttributeCollection attributes);
}