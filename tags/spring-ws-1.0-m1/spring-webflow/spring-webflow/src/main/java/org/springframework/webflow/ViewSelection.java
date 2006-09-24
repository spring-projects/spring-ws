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

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Abstract base class for value objects that provide SWF callers with
 * information about a logical response to issue and the data necessary to issue
 * it.
 * <p>
 * This class is a generic marker returned from a request into an executing flow
 * has completed processing, indicating a client response needs to be issued. An
 * instance of a ViewSelection subclass represents the selection of a concrete
 * response type. It is expected that callers introspect the returned view
 * selection instance to handle the response types they support.
 * <p>
 * View selections are returned as a result of entering a {@link ViewState} or
 * {@link EndState}, typically created by those states delegating to a
 * {@link ViewSelector} factory (a creational strategy). When a state of either
 * of those types is entered and returns, the caller into the web flow system is
 * handed a fully-configured <code>ViewSelection</code> instance and is
 * expected to present some form inteface to the client that allows for
 * interaction at that point within the flow.
 * 
 * @see org.springframework.webflow.ViewSelector
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class ViewSelection implements Serializable {

	/**
	 * Constant for a <code>null</code> or empty view selection, indicating no
	 * response should be issued.
	 */
	public static final ViewSelection NULL_VIEW = new NullViewSelection();

	/**
	 * The definition of the 'null' view selection type, indicating that no
	 * response should be issued.
	 * @author Keith Donald
	 */
	private static final class NullViewSelection extends ViewSelection {
		// resolve the singleton instance
		private Object readResolve() throws ObjectStreamException {
			return NULL_VIEW;
		}
	}
	
}