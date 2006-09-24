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
package org.springframework.webflow.test;

import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowExecutionControlContext;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.FlowSessionStatus;
import org.springframework.webflow.State;
import org.springframework.webflow.ViewSelection;

/**
 * Mock implementation of the <code>FlowControlContext</code> interface to
 * facilitate standalone Flow and State unit tests.
 * <p>
 * NOT intended to be used for anything but standalone unit tests. This is a
 * simple state holder, a <i>stub</i> implementation, at least if you follow <a
 * href="http://www.martinfowler.com/articles/mocksArentStubs.html">Martin
 * Fowler's</a> reasoning. This class is called <i>Mock</i>FlowControlContext
 * to be consistent with the naming convention in the rest of the Spring
 * framework (e.g. MockHttpServletRequest, ...).
 * 
 * @see org.springframework.webflow.RequestContext
 * @see org.springframework.webflow.FlowSession
 * @see org.springframework.webflow.State
 * 
 * @author Keith Donald
 */
public class MockFlowExecutionControlContext extends MockRequestContext implements FlowExecutionControlContext {

	/**
	 * Creates a new mock control context for controlling a mock execution of the
	 * provided flow definition.
	 */
	public MockFlowExecutionControlContext(Flow rootFlow) {
		super(rootFlow);
	}
	
	public ViewSelection start(Flow flow, AttributeMap input) throws IllegalStateException {
		getMockFlowExecutionContext().setActiveSession(new MockFlowSession(flow, input));
		getMockFlowExecutionContext().getMockActiveSession().setStatus(FlowSessionStatus.STARTING);
		ViewSelection selectedView = flow.start(this, input);
		return selectedView;
	}

	public ViewSelection signalEvent(Event event) {
		setLastEvent(event);
		ViewSelection selectedView = getActiveFlow().onEvent(event, this);
		return selectedView;
	}

	public FlowSession endActiveFlowSession(AttributeMap output) throws IllegalStateException {
		MockFlowSession endingSession = getMockFlowExecutionContext().getMockActiveSession();
		endingSession.getFlow().end(this, output);
		endingSession.setStatus(FlowSessionStatus.ENDED);
		getMockFlowExecutionContext().setActiveSession(null);
		return endingSession;
	}

	public void setCurrentState(State state) {
		getMockFlowExecutionContext().getMockActiveSession().setState(state);
		State previousState = getCurrentState();
		if (previousState == null) {
			getMockFlowExecutionContext().getMockActiveSession().setStatus(FlowSessionStatus.ACTIVE);
		}
	}
}