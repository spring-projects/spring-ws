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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An attribute map that encapsulates mutable {@link Map} operations. Useful for
 * passing around when modification of the target map should be always
 * disallowed.
 * 
 * @author Keith Donald
 */
public class UnmodifiableAttributeMap extends AbstractAttributeMap {

	/**
	 * Creates a new attribute map, initially empty.
	 */
	public UnmodifiableAttributeMap(Map attributes) {
		if (attributes == null) {
			attributes = Collections.EMPTY_MAP;
		}
		initAttributes(attributes);
	}

	public boolean equals(Object o) {
		if (!(o instanceof UnmodifiableAttributeMap)) {
			return false;
		}
		UnmodifiableAttributeMap other = (UnmodifiableAttributeMap)o;
		return getMapInternal().equals(other.getMapInternal());
	}
	
	public int hashCode() {
		return getMapInternal().hashCode();
	}

	public UnmodifiableAttributeMap unmodifiable() {
		return this;
	}

	public AttributeCollection union(AttributeCollection attributes) {
		if (attributes == null) {
			return this;
		} else {
			Map map = new HashMap();
			map.putAll(getMapInternal());
			map.putAll(attributes.getMap());
			return new UnmodifiableAttributeMap(map);
		}
	}
}