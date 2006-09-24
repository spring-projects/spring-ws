package org.springframework.webflow.executor.support;

import junit.framework.TestCase;

import org.springframework.webflow.EndState;
import org.springframework.webflow.Flow;
import org.springframework.webflow.Transition;
import org.springframework.webflow.ViewState;
import org.springframework.webflow.executor.FlowExecutorImpl;
import org.springframework.webflow.executor.ResponseInstruction;
import org.springframework.webflow.registry.FlowRegistryImpl;
import org.springframework.webflow.registry.StaticFlowHolder;
import org.springframework.webflow.support.DefaultTargetStateResolver;
import org.springframework.webflow.test.MockExternalContext;

public class FlowRequestHandlerTests extends TestCase {

	private FlowRequestHandler handler;

	private MockExternalContext context = new MockExternalContext();

	protected void setUp() throws Exception {
		FlowRegistryImpl registry = new FlowRegistryImpl();
		Flow flow = new Flow("flow");
		ViewState view = new ViewState(flow, "view");
		view.getTransitionSet().add(new Transition(new DefaultTargetStateResolver("end")));
		new EndState(flow, "end");
		registry.registerFlow(new StaticFlowHolder(flow));
		FlowExecutorImpl executor = new FlowExecutorImpl(registry);
		handler = new FlowRequestHandler(executor);
	}

	public void testLaunch() {
		context.putRequestParameter("_flowId", "flow");
		ResponseInstruction response = handler.handleFlowRequest(context);
		assertTrue(response.isNull());
		assertTrue(response.getFlowExecutionContext().isActive());
		assertEquals("flow", response.getFlowExecutionContext().getFlow().getId());
		assertEquals("view", response.getFlowExecutionContext().getActiveSession().getState().getId());
	}

	public void testResumeOnEvent() {
		context.putRequestParameter("_flowId", "flow");
		ResponseInstruction response = handler.handleFlowRequest(context);

		String flowExecutionKey = response.getFlowExecutionKey();
		context.putRequestParameter("_flowExecutionKey", flowExecutionKey);
		context.putRequestParameter("_eventId", "submit");
		response = handler.handleFlowRequest(context);

		assertTrue(response.isNull());
		assertTrue(!response.getFlowExecutionContext().isActive());
		assertEquals("flow", response.getFlowExecutionContext().getFlow().getId());

	}

	public void testRefreshFlowExecution() {
		context.putRequestParameter("_flowId", "flow");
		ResponseInstruction response = handler.handleFlowRequest(context);

		String flowExecutionKey = response.getFlowExecutionKey();
		context.putRequestParameter("_flowExecutionKey", flowExecutionKey);
		response = handler.handleFlowRequest(context);

		assertTrue(response.isNull());
		assertTrue(response.getFlowExecutionContext().isActive());
		assertEquals("flow", response.getFlowExecutionContext().getFlow().getId());
		assertEquals("view", response.getFlowExecutionContext().getActiveSession().getState().getId());
	}
}