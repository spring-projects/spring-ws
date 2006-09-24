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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A helper that performs action execution, encapsulating common logging and
 * exception handling logic. This is an internal helper class that is not
 * normally used by application code.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ActionExecutor {

	private static final Log logger = LogFactory.getLog(ActionExecutor.class);

	private ActionExecutor() {
	}

	/**
	 * Execute the wrapped action.
	 * @param context the flow execution request context
	 * @return result of action execution
	 * @throws ActionExecutionException if the action threw an exception while
	 * executing
	 */
	public static Event execute(Action action, RequestContext context) throws ActionExecutionException {
		try {
			if (logger.isDebugEnabled()) {
				if (context.getCurrentState() == null) {
					logger.debug("Executing start " + action + " for flow '" + context.getActiveFlow().getId() + "'");
				}
				else {
					logger.debug("Executing " + action + " in state '" + context.getCurrentState().getId()
							+ "' of flow '" + context.getActiveFlow().getId() + "'");
				}
			}
			return action.execute(context);
		}
		catch (ActionExecutionException e) {
			throw e;
		}
		catch (Exception e) {
			// wrap the action as an ActionExecutionException
			if (context.getCurrentState() == null) {
				throw new ActionExecutionException(context.getActiveFlow(), action, context.getAttributes(), e);
			}
			else {
				throw new ActionExecutionException(context.getCurrentState(), action, context.getAttributes(), e);
			}
		}
	}
}