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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.springframework.core.style.StylerUtils;

/**
 * A typed set of state exception handlers, mainly for use internally by
 * artifacts that can apply state exception handling logic.
 * 
 * @see Flow#getExceptionHandlerSet()
 * @see State#getExceptionHandlerSet()
 * 
 * @author Keith Donald
 */
public class StateExceptionHandlerSet {

	/**
	 * The set of exception handlers.
	 */
	private List exceptionHandlers = new LinkedList();

	/**
	 * Add a state exception handler to this set.
	 * @param exceptionHandler the exception handler to add
	 * @return true if this set's contents changed as a result of the add
	 * operation
	 */
	public boolean add(StateExceptionHandler exceptionHandler) {
		if (contains(exceptionHandler)) {
			return false;
		}
		return exceptionHandlers.add(exceptionHandler);
	}

	/**
	 * Add a collection of state exception handler instances to this set.
	 * @param exceptionHandlers the exception handlers to add
	 * @return true if this set's contents changed as a result of the add
	 * operation
	 */
	public boolean addAll(StateExceptionHandler[] exceptionHandlers) {
		if (exceptionHandlers == null) {
			return false;
		}
		boolean changed = false;
		for (int i = 0; i < exceptionHandlers.length; i++) {
			if (add(exceptionHandlers[i]) && !changed) {
				changed = true;
			}
		}
		return changed;
	}

	/**
	 * Tests if this state exception handler is in this set.
	 * @param exceptionHandler the exception handler
	 * @return true if the state exception handler is contained in this set,
	 * false otherwise
	 */
	public boolean contains(StateExceptionHandler exceptionHandler) {
		return exceptionHandlers.contains(exceptionHandler);
	}

	/**
	 * Remove the exception handler instance from this set.
	 * @param exceptionHandler the exception handler to add
	 * @return true if this set's contents changed as a result of the remove
	 * operation
	 */
	public boolean remove(StateExceptionHandler exceptionHandler) {
		return exceptionHandlers.remove(exceptionHandler);
	}

	/**
	 * Returns the size of this state exception handler set.
	 * @return the exception handler set size.
	 */
	public int size() {
		return exceptionHandlers.size();
	}

	/**
	 * Convert this list to a typed state exception handler array.
	 * @return the exception handler list, as a typed array
	 */
	public StateExceptionHandler[] toArray() {
		return (StateExceptionHandler[])exceptionHandlers.toArray(new StateExceptionHandler[exceptionHandlers.size()]);
	}

	/**
	 * Handle an exception that occured during the context of the current flow
	 * execution request.
	 * <p>
	 * This implementation iterates over the ordered set of exception handler
	 * objects, delegating to each handler in the set until one handles the
	 * exception that occured and selects a non-null error view.
	 * @param exception the exception that occured
	 * @param context the flow execution control context
	 * @return the selected error view, or <code>null</code> if no handler
	 * matched or returned a non-null view selection
	 */
	public ViewSelection handleException(StateException exception, FlowExecutionControlContext context) {
		Iterator it = exceptionHandlers.iterator();
		while (it.hasNext()) {
			StateExceptionHandler handler = (StateExceptionHandler)it.next();
			if (handler.handles(exception)) {
				return handler.handle(exception, context);
			}
		}
		return null;
	}

	public String toString() {
		return StylerUtils.style(exceptionHandlers);
	}
}