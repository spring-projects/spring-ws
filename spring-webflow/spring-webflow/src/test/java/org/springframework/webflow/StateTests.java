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

import org.springframework.webflow.test.MockFlowExecutionControlContext;

/**
 * Tests that each of the Flow state types execute as expected when entered.
 * 
 * @author Keith Donald
 */
public class StateTests extends TestCase {

	private Flow flow;

	private State state;

	private boolean entered;

	public void setUp() {
		flow = new Flow("flow");
		state = new State(flow, "myState") {
			protected ViewSelection doEnter(FlowExecutionControlContext context) throws StateException {
				entered = true;
				return ViewSelection.NULL_VIEW;
			}
		};
	}

	public void testStateEnter() {
		assertEquals("myState", state.getId());
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		state.enter(context);
		assertEquals(state, context.getCurrentState());
	}

	public void testStateEnterWithEntryAction() {
		TestAction action = new TestAction();
		state.getEntryActionList().add(action);
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		state.enter(context);
		assertEquals(state, context.getCurrentState());
		assertTrue(action.isExecuted());
		assertEquals(1, action.getExecutionCount());
	}
}
