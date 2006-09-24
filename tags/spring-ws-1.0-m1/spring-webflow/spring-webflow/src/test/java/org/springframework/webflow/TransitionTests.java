package org.springframework.webflow;

import junit.framework.TestCase;

import org.springframework.webflow.support.DefaultTargetStateResolver;
import org.springframework.webflow.support.EventIdTransitionCriteria;
import org.springframework.webflow.test.MockFlowExecutionControlContext;

public class TransitionTests extends TestCase {

	public void testSimpleTransition() {
		Transition t = new Transition(new DefaultTargetStateResolver("target"));
		Flow flow = new Flow("flow");
		ViewState source = new ViewState(flow, "source");
		TestAction action = new TestAction();
		source.getExitActionList().add(action);
		ViewState target = new ViewState(flow, "target");
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		context.setCurrentState(source);
		t.execute(source, context);
		assertTrue(t.matches(context));
		assertEquals(t, context.getLastTransition());
		assertEquals(context.getCurrentState(), target);
		assertEquals(1, action.getExecutionCount());
	}

	public void testTransitionCriteriaDoesNotMatch() {
		Transition t = new Transition(new EventIdTransitionCriteria("bogus"), new DefaultTargetStateResolver("target"));
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(new Flow("flow"));
		assertFalse(t.matches(context));
	}

	public void testTransitionCannotExecute() {
		Transition t = new Transition(new DefaultTargetStateResolver("target"));
		t.setExecutionCriteria(new EventIdTransitionCriteria("bogus"));
		Flow flow = new Flow("flow");
		ViewState source = new ViewState(flow, "source");
		TestAction action = new TestAction();
		source.getExitActionList().add(action);
		ViewState target = new ViewState(flow, "target");
		MockFlowExecutionControlContext context = new MockFlowExecutionControlContext(flow);
		context.setCurrentState(source);
		t.execute(source, context);
		assertTrue(t.matches(context));
		assertEquals(null, context.getLastTransition());
		assertEquals(context.getCurrentState(), source);
		assertEquals(0, action.getExecutionCount());
	}
}