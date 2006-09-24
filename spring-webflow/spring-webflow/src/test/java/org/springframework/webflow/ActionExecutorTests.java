package org.springframework.webflow;

import junit.framework.TestCase;

import org.springframework.webflow.test.MockFlowSession;
import org.springframework.webflow.test.MockRequestContext;

public class ActionExecutorTests extends TestCase {

	public void testBasicExecute() {
		TestAction action = new TestAction();
		Event result = ActionExecutor.execute(action, new MockRequestContext());
		assertEquals("success", result.getId());
	}

	public void testExceptionWhileStarted() {
		TestAction action = new TestAction() {
			protected Event doExecute(RequestContext context) throws Exception {
				throw new IllegalStateException("Oops");
			}
		};
		try {
			ActionExecutor.execute(action, new MockRequestContext());
			fail("Should've failed");
		}
		catch (ActionExecutionException e) {
			assertTrue(e.getCause() instanceof IllegalStateException);
		}
	}

	public void testExceptionWhileStarting() {
		TestAction action = new TestAction() {
			protected Event doExecute(RequestContext context) throws Exception {
				throw new IllegalStateException("Oops");
			}
		};
		MockRequestContext context = new MockRequestContext();
		MockFlowSession starting = new MockFlowSession(new Flow("flow"));
		starting.setStatus(FlowSessionStatus.STARTING);
		context.getMockFlowExecutionContext().setActiveSession(starting);
		try {
			ActionExecutor.execute(action, context);
			fail("Should've failed");
		}
		catch (ActionExecutionException e) {
			assertTrue(e.getCause() instanceof IllegalStateException);
		}
	}
}
