/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow;

public class SharedAttributeMap extends AttributeMap {

	/**
	 * Creates a new shared attribute map.
	 * @param sharedMap the shared map
	 */
	public SharedAttributeMap(SharedMap sharedMap) {
		super(sharedMap);
	}

	/**
	 * Returns the wrapped shared map.
	 */
	public SharedMap getSharedMap() {
		return (SharedMap)getMapInternal();
	}

	/**
	 * Returns the shared map's mutex, which may be synchronized on to block
	 * access to the map by other threads.
	 */
	public Object getMutex() {
		return getSharedMap().getMutex();
	}
}