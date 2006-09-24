package org.springframework.webflow.executor.support;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.support.FlowRedirect;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockFlowExecutionContext;

public class RequestPathFlowExecutorArgumentExtractorTests extends TestCase {
	private MockExternalContext context = new MockExternalContext();

	private RequestPathFlowExecutorArgumentExtractor argumentExtractor;

	private String flowExecutionKey;

	public void setUp() {
		argumentExtractor = new RequestPathFlowExecutorArgumentExtractor();
		flowExecutionKey = "_c12345_k12345";
	}

	public void testExtractFlowId() {
		MockExternalContext context = new MockExternalContext();
		context.setRequestPathInfo("flow");
		assertEquals("flow", argumentExtractor.extractFlowId(context));
	}

	public void testExtractFlowIdDefault() {
		argumentExtractor.setDefaultFlowId("flow");
		assertEquals("flow", argumentExtractor.extractFlowId(new MockExternalContext()));
	}

	public void testExtractFlowIdNoRequestPath() {
		try {
			argumentExtractor.extractFlowId(new MockExternalContext());
			fail("should've failed");
		}
		catch (FlowExecutorArgumentExtractionException e) {

		}
	}

	public void testCreateFlowUrl() {
		context.setContextPath("/app");
		context.setDispatcherPath("/flows");
		FlowRedirect flowRedirect = new FlowRedirect("flow", null);
		String url = argumentExtractor.createFlowUrl(flowRedirect, context);
		assertEquals("/app/flows/flow", url);
	}

	public void testCreateFlowUrlInput() {
		context.setContextPath("/app");
		context.setDispatcherPath("/flows");
		Map input = new HashMap();
		input.put("foo", "bar");
		input.put("baz", new Integer(3));
		FlowRedirect flowRedirect = new FlowRedirect("flow", input);
		String url = argumentExtractor.createFlowUrl(flowRedirect, context);
		assertEquals("/app/flows/flow?foo=bar&baz=3", url);
	}

	public void testCreateFlowUrlInputRequestPath() {
		context.setContextPath("/app");
		context.setDispatcherPath("/flows");
		Map input = new HashMap();
		input.put("foo", "bar");
		input.put("baz", new Integer(3));
		FlowRedirect flowRedirect = new FlowRedirect("flow", input);
		argumentExtractor.setAppendFlowInputAttributesToRequestPath(true);
		String url = argumentExtractor.createFlowUrl(flowRedirect, context);
		assertEquals("/app/flows/flow/bar/3", url);
	}

	public void testCreateFlowExecutionUrl() {
		context.setContextPath("/app");
		context.setDispatcherPath("/flows");
		FlowExecutionContext flowExecution = new MockFlowExecutionContext();
		String url = argumentExtractor.createFlowExecutionUrl(flowExecutionKey, flowExecution, context);
		assertEquals("/app/flows/mockFlow?_flowExecutionKey=_c12345_k12345", url);
	}
}