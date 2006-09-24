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
package org.springframework.webflow.action;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.Action;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.TransitionCriteria;

/**
 * An action that will execute another action if a guard allows it to.
 * The guard is represented by a TransitionCriteria object. If the guard
 * disallows execution, the wrapped action is just skipped and this
 * action will return the "success" event.
 * 
 * @see org.springframework.webflow.TransitionCriteria
 * 
 * @author Keith Donald
 */
public class GuardedAction extends AbstractAction {

	/**
	 * The actions that *may* be executed.
	 */
	private Action action;

	/**
	 * The action execution criteria.
	 */
	private TransitionCriteria executionCriteria;
	
	/**
	 * Create a guarded action.
	 * @param action the action to execute
	 * @param executionCriteria the guard to use
	 */
	public GuardedAction(Action action, TransitionCriteria executionCriteria) {
		Assert.notNull(action, "The action is required");
		Assert.notNull(executionCriteria, "The guarding execution criteria is required");
		this.action = action;
		this.executionCriteria = executionCriteria;
	}

	/**
	 * Returns the guarded action.
	 * @return the action
	 */
	public Action getAction() {
		return action;
	}
	
	/**
	 * Returns the action execution criteria.
	 * @return the execution criteria
	 */
	public TransitionCriteria getExecutionCriteria() {
		return executionCriteria;
	}

	public Event doExecute(RequestContext context) throws Exception {
		if (getExecutionCriteria().test(context)) {
			return getAction().execute(context);
		}
		else {
			// skip wrapped action
			return success();
		}
	}
	
	public String toString() {
		return new ToStringCreator(this).append("action", getAction()).append("executionCriteria", executionCriteria).toString();
	}
}