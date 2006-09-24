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

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.Action;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;

/**
 * An action that will execute an ordered chain of other actions when executed.
 * The result of the last executed action is returned. This is the classic GoF
 * composite design pattern.
 * 
 * @author Keith Donald
 */
public class CompositeAction extends AbstractAction {

	/**
	 * The actions to execute.
	 */
	private Action[] actions;

	/**
	 * Should execution stop if one action returns an error event?
	 */
	private boolean stopOnError;

	/**
	 * Create a composite action composed of given actions.
	 * @param actions the actions
	 */
	public CompositeAction(Action[] actions) {
		Assert.notEmpty(actions, "At least one action is required");
		this.actions = actions;
	}

	/**
	 * Returns the actions contained by this composite action.
	 * @return the actions
	 */
	protected Action[] getActions() {
		return actions;
	}

	/**
	 * Returns the stop on error flag.
	 */
	public boolean isStopOnError() {
		return stopOnError;
	}

	/**
	 * Sets the stop on error flag.
	 */
	public void setStopOnError(boolean stopOnError) {
		this.stopOnError = stopOnError;
	}

	public Event doExecute(RequestContext context) throws Exception {
		Action[] actions = getActions();
		String eventId = getEventFactorySupport().getSuccessEventId();
		AttributeMap eventAttributes = new AttributeMap();
		List actionResults = new ArrayList(actions.length);
		for (int i = 0; i < actions.length; i++) {
			Event result = actions[i].execute(context);
			if (result != null) {
				actionResults.add(result);
				if (isStopOnError() && result != null
						&& result.getId().equals(getEventFactorySupport().getErrorEventId())) {
					eventId = getEventFactorySupport().getErrorEventId();
					break;
				}
			}
		}
		eventAttributes.put("actionResults", actionResults);
		return new Event(this, eventId, eventAttributes);
	}

	public String toString() {
		return new ToStringCreator(this).append("actions", getActions()).append("stopOnError", isStopOnError())
				.toString();
	}
}