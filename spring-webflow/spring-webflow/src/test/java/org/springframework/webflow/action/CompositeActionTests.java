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
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.Event;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Unit tests for the CompositeAction class.
 * 
 * @author Ulrik Sandberg
 */
public class CompositeActionTests extends TestCase {

	private CompositeAction tested;

	private MockControl actionControl;

	private Action actionMock;

	protected void setUp() throws Exception {
		super.setUp();
		actionControl = MockControl.createControl(Action.class);
		actionMock = (Action)actionControl.getMock();
		Action[] actions = new Action[] { actionMock };
		tested = new CompositeAction(actions);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		actionControl = null;
		actionMock = null;
		tested = null;
	}

	public void testDoExecute() throws Exception {
		MockRequestContext mockRequestContext = new MockRequestContext();
		AttributeMap attributes = new AttributeMap();
		attributes.put("some key", "some value");
		actionControl
				.expectAndReturn(actionMock.execute(mockRequestContext), new Event(this, "some event", attributes));
		actionControl.replay();
		Event result = tested.doExecute(mockRequestContext);
		actionControl.verify();
		assertEquals("success", result.getId());
		assertEquals(1, result.getAttributes().size());
	}

	public void testDoExecuteWithError() throws Exception {
		tested.setStopOnError(true);
		MockRequestContext mockRequestContext = new MockRequestContext();
		actionControl.expectAndReturn(actionMock.execute(mockRequestContext), new Event(this, "error"));
		actionControl.replay();
		Event result = tested.doExecute(mockRequestContext);
		actionControl.verify();
		assertEquals("error", result.getId());
	}

	public void testDoExecuteWithNullResult() throws Exception {
		tested.setStopOnError(true);
		MockRequestContext mockRequestContext = new MockRequestContext();
		actionControl.expectAndReturn(actionMock.execute(mockRequestContext), null);
		actionControl.replay();
		Event result = tested.doExecute(mockRequestContext);
		actionControl.verify();
		assertEquals("Expecting success since no check is performed if null result,", "success", result.getId());
	}
}