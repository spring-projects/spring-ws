/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow.support;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.webflow.Action;
import org.springframework.webflow.Event;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Unit tests for the ActionTransitionCriteria class.
 * 
 * @author Ulrik Sandberg
 */
public class ActionTransitionCriteriaTests extends TestCase {

	private MockControl actionControl;

	private Action actionMock;

	private ActionTransitionCriteria tested;

	protected void setUp() throws Exception {
		super.setUp();
		actionControl = MockControl.createControl(Action.class);
		actionMock = (Action)actionControl.getMock();
		tested = new ActionTransitionCriteria(actionMock);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		actionControl = null;
		actionMock = null;
		tested = null;
	}

	public void testGetTrueEventId() {
		String id = tested.getTrueEventId();
		assertEquals("success", id);
	}

	public void testSetTrueEventId() {
		tested.setTrueEventId("something");
		String id = tested.getTrueEventId();
		assertEquals("something", id);
	}

	public void testGetAction() {
		Action action = tested.getAction();
		assertSame(actionMock, action);
	}

	public void testTest() throws Exception {
		MockRequestContext mockRequestContext = new MockRequestContext();
		actionControl.expectAndReturn(actionMock.execute(mockRequestContext), new Event(this, "success"));
		actionControl.replay();

		boolean result = tested.test(mockRequestContext);

		actionControl.verify();
		assertEquals(true, result);
	}
}