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

/**
 * A strategy for handling an exception that occurs in a state during the
 * execution of a flow definition.
 * 
 * @author Keith Donald
 */
public interface StateExceptionHandler {

	/**
	 * Can this handler handle the given exception?
	 * @param exception the exception that occured
	 * @return true if yes, false if no
	 */
	public boolean handles(StateException exception);

	/**
	 * Handle this exception in the context of the current request and
	 * optionally select an error view that should be displayed.
	 * @param exception the exception that occured
	 * @param context the flow execution control context
	 * @return the selected error view that should be displayed (may be null if
	 * the handler chooses not to select a view, in which case other exception
	 * handlers will be given a chance to handle the exception)
	 */
	public ViewSelection handle(StateException exception, FlowExecutionControlContext context);
}
