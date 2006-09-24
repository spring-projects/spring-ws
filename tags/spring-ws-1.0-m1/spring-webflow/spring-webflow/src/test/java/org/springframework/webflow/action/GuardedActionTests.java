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
package org.springframework.webflow.action;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.webflow.Action;
import org.springframework.webflow.Event;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Unit tests for the GuardedAction class.
 * 
 * @author Ulrik Sandberg
 */
public class GuardedActionTests extends TestCase {

	private MockControl transitionCriteriaControl;

	private TransitionCriteria transitionCriteriaMock;

	private MockControl actionControl;

	private Action actionMock;

	protected void setUp() throws Exception {
		super.setUp();
		actionControl = MockControl.createControl(Action.class);
		actionMock = (Action)actionControl.getMock();
		transitionCriteriaControl = MockControl.createControl(TransitionCriteria.class);
		transitionCriteriaMock = (TransitionCriteria)transitionCriteriaControl.getMock();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		actionControl = null;
		actionMock = null;
		transitionCriteriaControl = null;
		transitionCriteriaMock = null;
	}

	protected void replay() {
		actionControl.replay();
		transitionCriteriaControl.replay();
	}

	protected void verify() {
		actionControl.verify();
		transitionCriteriaControl.verify();
	}

	public void testInit() throws Exception {
		GuardedAction tested = new GuardedAction(actionMock, transitionCriteriaMock);
		assertNotNull("Property 'action' should not be null. Was it set correctly in the constructor?", tested
				.getAction());
		assertNotNull("Property 'executionCriteria' should not be null. Was it set correctly in the constructor?",
				tested.getExecutionCriteria());
	}

	public void testDoExecuteNullAction() {
		try {
			new GuardedAction(null, transitionCriteriaMock);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertEquals("The action is required", expected.getMessage());
		}
	}

	public void testDoExecuteNullCriteria() {
		try {
			new GuardedAction(actionMock, null);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertEquals("The guarding execution criteria is required", expected.getMessage());
		}
	}

	public void testDoExecuteCriteriaFalse() throws Exception {
		MockRequestContext mockRequestContext = new MockRequestContext();
		transitionCriteriaControl.expectAndReturn(transitionCriteriaMock.test(mockRequestContext), false);
		replay();
		GuardedAction tested = new GuardedAction(actionMock, transitionCriteriaMock);
		Event result = tested.doExecute(mockRequestContext);
		verify();
		assertEquals("success", result.getId());
	}

	public void testDoExecuteCriteriaTrue() throws Exception {
		MockRequestContext mockRequestContext = new MockRequestContext();
		transitionCriteriaControl.expectAndReturn(transitionCriteriaMock.test(mockRequestContext), true);
		actionControl.expectAndReturn(actionMock.execute(mockRequestContext), new Event(this, "myevent"));
		replay();
		GuardedAction tested = new GuardedAction(actionMock, transitionCriteriaMock);
		Event result = tested.doExecute(mockRequestContext);
		verify();
		assertEquals("myevent", result.getId());
	}
}