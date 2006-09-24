package org.springframework.webflow.executor;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.Flow;
import org.springframework.webflow.NoMatchingTransitionException;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import org.springframework.webflow.registry.NoSuchFlowDefinitionException;
import org.springframework.webflow.support.ApplicationView;
import org.springframework.webflow.test.MockExternalContext;

public class FlowExecutorIntegrationTests extends AbstractDependencyInjectionSpringContextTests {

	private FlowExecutor flowExecutor;

	public void setFlowExecutor(FlowExecutor flowExecutor) {
		this.flowExecutor = flowExecutor;
	}

	protected String[] getConfigLocations() {
		return new String[] { "org/springframework/webflow/executor/context.xml" };
	}

	public void testConfigurationOk() {
		assertNotNull(flowExecutor);
	}

	public void testLaunchFlow() {
		ExternalContext context = new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(),
				new MockHttpServletResponse());
		ResponseInstruction response = flowExecutor.launch("flow", context);
		assertTrue(response.getFlowExecutionContext().isActive());
		assertEquals("viewState1", response.getFlowExecutionContext().getActiveSession().getState().getId());
		assertTrue(response.isApplicationView());
		ApplicationView view = (ApplicationView)response.getViewSelection();
		assertEquals("view1", view.getViewName());
		assertEquals(0, view.getModel().size());
	}

	public void testLaunchNoSuchFlow() {
		try {
			ExternalContext context = new ServletExternalContext(new MockServletContext(),
					new MockHttpServletRequest(), new MockHttpServletResponse());
			flowExecutor.launch("bogus", context);
			fail("no such flow expected");
		}
		catch (NoSuchFlowDefinitionException e) {
			assertEquals("bogus", e.getArtifactId());
			assertEquals(Flow.class, e.getArtifactType());
		}
	}

	public void testLaunchAndSignalEvent() {
		ExternalContext context = new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(),
				new MockHttpServletResponse());
		ResponseInstruction response = flowExecutor.launch("flow", context);
		String key = response.getFlowExecutionKey();
		assertEquals("viewState1", response.getFlowExecutionContext().getActiveSession().getState().getId());
		response = flowExecutor.signalEvent("event1", key, context);
		assertTrue(response.getFlowExecutionContext().isActive());
		assertEquals("viewState2", response.getFlowExecutionContext().getActiveSession().getState().getId());
		assertTrue(response.isApplicationView());
		assertNotNull(response.getFlowExecutionKey());
		ApplicationView view = (ApplicationView)response.getViewSelection();
		assertEquals("view2", view.getViewName());
		assertEquals(0, view.getModel().size());
		response = flowExecutor.signalEvent("event1", response.getFlowExecutionKey(), context);
		view = (ApplicationView)response.getViewSelection();
		assertFalse(response.getFlowExecutionContext().isActive());
		assertTrue(response.isApplicationView());
		assertNull(response.getFlowExecutionKey());
		assertEquals("endView1", view.getViewName());
		assertEquals(0, view.getModel().size());
		try {
			flowExecutor.signalEvent("event1", key, context);
			fail("Should've been removed");
		}
		catch (NoSuchFlowExecutionException e) {

		}
	}

	public void testRefresh() {
		ExternalContext context = new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(),
				new MockHttpServletResponse());
		ResponseInstruction response = flowExecutor.launch("flow", context);
		ResponseInstruction response2 = flowExecutor.refresh(response.getFlowExecutionKey(), context);
		assertEquals(response, response2);
	}

	public void testNoSuchFlowExecution() {
		try {
			flowExecutor.signalEvent("bogus", "_cbogus_kbogus", new MockExternalContext());
			fail("Should've failed");
		}
		catch (NoSuchFlowExecutionException e) {
			assertEquals("_cbogus_kbogus", e.getFlowExecutionKey().toString());
		}
	}

	public void testSignalEventNoMatchingTransition() {
		ExternalContext context = new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(),
				new MockHttpServletResponse());
		ResponseInstruction response = flowExecutor.launch("flow", context);
		String key = response.getFlowExecutionKey();
		try {
			flowExecutor.signalEvent("bogus", key, context);
			fail("Should've been removed");
		}
		catch (NoMatchingTransitionException e) {
			assertEquals("flow", e.getFlow().getId());
			assertEquals("viewState1", e.getState().getId());
			assertEquals("bogus", e.getEvent().getId());
		}
	}
}