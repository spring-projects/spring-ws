/*
 * Copyright 2002-2004 the original author or authors.
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

import junit.framework.TestCase;

import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.impl.FlowExecutionImpl;
import org.springframework.webflow.support.ApplicationView;
import org.springframework.webflow.support.ApplicationViewSelector;
import org.springframework.webflow.support.DefaultTargetStateResolver;
import org.springframework.webflow.support.EventIdTransitionCriteria;
import org.springframework.webflow.test.MockExternalContext;

/**
 * Tests that each of the Flow state types execute as expected when entered.
 * 
 * @author Keith Donald
 */
public class ViewStateTests extends TestCase {

	public void testViewState() {
		Flow flow = new Flow("myFlow");
		ViewState state = new ViewState(flow, "viewState");
		state.setViewSelector(view("myViewName"));
		state.getTransitionSet().add(new Transition(on("submit"), to("finish")));
		new EndState(flow, "finish");
		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		ApplicationView view = (ApplicationView)flowExecution.start(null, new MockExternalContext());
		assertEquals("viewState", flowExecution.getActiveSession().getState().getId());
		assertNotNull(view);
		assertEquals("myViewName", view.getViewName());
	}

	public void testViewStateMarker() {
		Flow flow = new Flow("myFlow");
		ViewState state = new ViewState(flow, "viewState");
		state.getTransitionSet().add(new Transition(on("submit"), to("finish")));
		new EndState(flow, "finish");
		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		ViewSelection view = flowExecution.start(null, new MockExternalContext());
		assertEquals("viewState", flowExecution.getActiveSession().getState().getId());
		assertEquals(ViewSelection.NULL_VIEW, view);
	}

	protected static TransitionCriteria on(String event) {
		return new EventIdTransitionCriteria(event);
	}

	protected static TargetStateResolver to(String stateId) {
		return new DefaultTargetStateResolver(stateId);
	}

	public static ViewSelector view(String viewName) {
		return new ApplicationViewSelector(new StaticExpression(viewName));
	}
}