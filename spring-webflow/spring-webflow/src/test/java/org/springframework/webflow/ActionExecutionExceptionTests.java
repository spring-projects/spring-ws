package org.springframework.webflow;

import junit.framework.TestCase;

public class ActionExecutionExceptionTests extends TestCase {
	public void testStateException() {
		Flow flow = new Flow("flow");
		ViewState state = new ViewState(flow, "state");
		TestAction action = new TestAction();
		UnmodifiableAttributeMap attributes = new AttributeMap().unmodifiable();
		ActionExecutionException e = new ActionExecutionException(state, action, attributes, null);
		assertSame(flow, e.getFlow());
		assertSame(state, e.getState());
		assertSame(attributes, e.getExecutionAttributes());
	}
	
	public void testFlowException() {
		Flow flow = new Flow("flow");
		TestAction action = new TestAction();
		UnmodifiableAttributeMap attributes = new AttributeMap().unmodifiable();
		ActionExecutionException e = new ActionExecutionException(flow, action, attributes, null);
		assertSame(flow, e.getFlow());
		assertSame(null, e.getState());
		assertSame(attributes, e.getExecutionAttributes());
	}
}
