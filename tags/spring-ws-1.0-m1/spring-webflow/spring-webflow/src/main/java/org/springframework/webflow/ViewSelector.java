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
 * Factory that produces a new, configured {@link ViewSelection} object on each
 * invocation, taking into account the information in the provided flow
 * execution request context.
 * <p>
 * Note: this class is a runtime factory. Instances are used at flow execution
 * time by objects like the {@link ViewState} to produce new
 * {@link ViewSelection view selections}
 * <p>
 * This class allows for easy insertion of dynamic view selection logic, for
 * instance, letting you determine the view to render or the available model
 * data for rendering based on contextual information.
 * 
 * @see org.springframework.webflow.ViewSelection
 * @see org.springframework.webflow.ViewState
 * @see org.springframework.webflow.EndState
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface ViewSelector {
	
	/**
	 * Make a new view selection for the given request context.
	 * @param context the current request context of the executing flow
	 * @return the view selection
	 */
	public ViewSelection makeSelection(RequestContext context);
	
	/**
	 * Reconstitute the view selection for the given request context 
	 * to support a ViewState 'refresh' operation.
	 * @param context the current request context of the executing flow
	 * @return the view selection
	 */
	public ViewSelection makeRefreshSelection(RequestContext context);

}