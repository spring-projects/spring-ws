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

import org.springframework.webflow.support.DefaultTargetStateResolver;
import org.springframework.webflow.support.EventIdTransitionCriteria;
import org.springframework.webflow.test.MockFlowExecutionControlContext;

/**
 * Tests that each of the Flow state types execute as expected when entered.
 * 
 * @author Keith Donald
 */
public class DecisionStateTests extends TestCase {

	public void testIfDecision() {
		Flow flow = new Flow("flow");
		DecisionState state = new DecisionState(flow, "decisionState");
		state.getTransitionSet().add(
				new Transition(new EventIdTransitionCriteria("foo"), new DefaultTargetStateResolver("target")));
		new EndState(flow, "target");
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		context.setLastEvent(new Event(this, "foo"));
		state.enter(context);
		assertFalse(context.getFlowExecutionContext().isActive());
	}

	public void testElseDecision() {
		Flow flow = new Flow("flow");
		DecisionState state = new DecisionState(flow, "decisionState");
		state.getTransitionSet().add(
				new Transition(new EventIdTransitionCriteria("foo"), new DefaultTargetStateResolver("invalid")));
		state.getTransitionSet().add(new Transition(new DefaultTargetStateResolver("target")));
		new EndState(flow, "target");
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		context.setLastEvent(new Event(this, "bogus"));
		state.enter(context);
		assertFalse(context.getFlowExecutionContext().isActive());
	}

	public void testNoMatching() {
		Flow flow = new Flow("flow");
		DecisionState state = new DecisionState(flow, "decisionState");
		state.getTransitionSet().add(
				new Transition(new EventIdTransitionCriteria("foo"), new DefaultTargetStateResolver("invalid")));
		state.getTransitionSet().add(
				new Transition(new EventIdTransitionCriteria("bar"), new DefaultTargetStateResolver("invalid")));
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		context.setLastEvent(new Event(this, "bogus"));
		try {
			state.enter(context);
			fail("Expected no matching");
		}
		catch (NoMatchingTransitionException e) {

		}
	}
}
