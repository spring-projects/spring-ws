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

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Makes a null view selection, indicating no response should be issued.
 * 
 * @author Keith Donald
 */
public final class NullViewSelector implements ViewSelector, Serializable {

	/**
	 * The shared singleton {@link NullViewSelector} instance. 
	 */
	public static final ViewSelector INSTANCE = new NullViewSelector();

	private NullViewSelector() {

	}

	public ViewSelection makeRefreshSelection(RequestContext context) {
		return makeSelection(context);
	}
	
	public ViewSelection makeSelection(RequestContext context) {
		return ViewSelection.NULL_VIEW;
	}

	// resolve the singleton instance
	private Object readResolve() throws ObjectStreamException {
		return INSTANCE;
	}
	
}