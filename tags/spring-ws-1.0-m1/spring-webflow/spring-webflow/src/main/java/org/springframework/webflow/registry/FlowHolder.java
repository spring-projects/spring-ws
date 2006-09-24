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
package org.springframework.webflow.registry;

import org.springframework.webflow.Flow;

/**
 * A holder holding a reference to a Flow definition. Provides a layer of
 * indirection for managing a refreshable Flow definition.
 * @author Keith Donald
 */
public interface FlowHolder {

	/**
	 * Returns the <code>id</code> of the flow definition held by this holder.
	 * This is a <i>lightweight</i> method callers may call to obtain the id of
	 * the Flow without triggering full Flow definition assembly (which may be
	 * an expensive operation).
	 */
	public String getId();

	/**
	 * Returns the Flow definition held by this holder. Calling this method the
	 * first time may trigger Flow assembly.
	 */
	public Flow getFlow();

	/**
	 * Refresh the Flow definition held by this holder. Calling this method
	 * typically triggers Flow reassembly, which may include a refresh from an
	 * externalized resource such as a file.
	 */
	public void refresh();
}