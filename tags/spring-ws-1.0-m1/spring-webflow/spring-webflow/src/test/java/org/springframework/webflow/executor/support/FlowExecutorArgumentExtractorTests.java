package org.springframework.webflow.executor.support;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.execution.EventId;
import org.springframework.webflow.support.ExternalRedirect;
import org.springframework.webflow.support.FlowRedirect;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockFlowExecutionContext;

public class FlowExecutorArgumentExtractorTests extends TestCase {
	private MockExternalContext context;

	private FlowExecutorArgumentExtractor argumentExtractor;

	private String flowExecutionKey;

	public void setUp() {
		context = new MockExternalContext();
		argumentExtractor = new FlowExecutorArgumentExtractor();
		flowExecutionKey = "_c12345_k12345";
	}

	public void testExtractFlowId() {
		context.putRequestParameter("_flowId", "flow");
		assertEquals("flow", argumentExtractor.extractFlowId(context));
	}

	public void testExtractFlowIdDefault() {
		argumentExtractor.setDefaultFlowId("flow");
		assertEquals("flow", argumentExtractor.extractFlowId(new MockExternalContext()));
	}

	public void testExtractFlowIdNoIdProvided() {
		try {
			argumentExtractor.extractFlowId(context);
			fail("no flow id provided");
		}
		catch (FlowExecutorArgumentExtractionException e) {

		}
	}

	public void testExtractFlowExecutionId() {
		context.putRequestParameter("_flowExecutionKey", "_c12345_k12345");
		assertEquals(flowExecutionKey, argumentExtractor.extractFlowExecutionKey(context));
	}

	public void testExtractFlowExecutionNoKeyProvided() {
		try {
			argumentExtractor.extractFlowExecutionKey(context);
			fail("no flow execution key provided");
		}
		catch (FlowExecutorArgumentExtractionException e) {

		}
	}

	public void testExtractEventId() {
		context.putRequestParameter("_eventId", "submit");
		assertEquals("submit", argumentExtractor.extractEventId(context));
	}

	public void testExtractEventIdButtonNameFormat() {
		context.putRequestParameter("_eventId_submit", "not important");
		context.putRequestParameter("_somethingElse", "not important");
		assertEquals("submit", argumentExtractor.extractEventId(context));
	}

	public void testExtractEventIdNoIdProvided() {
		try {
			argumentExtractor.extractEventId(context);
			fail("no event id provided");
		}
		catch (FlowExecutorArgumentExtractionException e) {

		}
	}

	public void testCreateFlowUrl() {
		context.setContextPath("/app");
		context.setDispatcherPath("/flows.htm");
		FlowRedirect flowRedirect = new FlowRedirect("flow", null);
		String url = argumentExtractor.createFlowUrl(flowRedirect, context);
		assertEquals("/app/flows.htm?_flowId=flow", url);
	}

	public void testCreateFlowUrlWithInput() {
		context.setContextPath("/app");
		context.setDispatcherPath("/flows.htm");
		Map input = new HashMap();
		input.put("foo", "bar");
		input.put("baz", new Integer(3));
		FlowRedirect flowRedirect = new FlowRedirect("flow", input);
		String url = argumentExtractor.createFlowUrl(flowRedirect, context);
		assertEquals("/app/flows.htm?_flowId=flow&foo=bar&baz=3", url);
	}

	public void testCreateFlowExecutionUrl() {
		context.setContextPath("/app");
		context.setDispatcherPath("/flows.htm");
		FlowExecutionContext flowExecution = new MockFlowExecutionContext();
		String url = argumentExtractor.createFlowExecutionUrl(flowExecutionKey, flowExecution, context);
		assertEquals("/app/flows.htm?_flowExecutionKey=_c12345_k12345", url);
	}

	public void testCreateExternalUrlAbsolute() {
		context.setContextPath("/app");
		context.setDispatcherPath("/flows.htm");
		ExternalRedirect redirect = new ExternalRedirect("/a/url");
		argumentExtractor.setRedirectContextRelative(false);
		String url = argumentExtractor.createExternalUrl(redirect, flowExecutionKey, context);
		assertEquals("/a/url?_flowExecutionKey=_c12345_k12345", url);
	}

	public void testCreateExternalUrlContextRelative() {
		context.setContextPath("/app");
		context.setDispatcherPath("/flows.htm");
		ExternalRedirect redirect = new ExternalRedirect("/a/url");
		String url = argumentExtractor.createExternalUrl(redirect, flowExecutionKey, context);
		assertEquals("/app/a/url?_flowExecutionKey=_c12345_k12345", url);
	}

	public void testCreateExternalUrlNoKey() {
		context.setContextPath("/app");
		context.setDispatcherPath("/flows");
		ExternalRedirect redirect = new ExternalRedirect("/a/url");
		String url = argumentExtractor.createExternalUrl(redirect, null, context);
		assertEquals("/app/a/url", url);
	}

	public void testCreateExternalUrlNoKeyRelativeUrl() {
		context.setContextPath("/app");
		context.setDispatcherPath("/flows");
		ExternalRedirect redirect = new ExternalRedirect("a/url");
		String url = argumentExtractor.createExternalUrl(redirect, null, context);
		assertEquals("a/url", url);
	}

	public void testAccidentalParameterArraySubmit() {
		context.putRequestParameter("_flowExecutionKey", new String[] { "_c12345_k12345", "_c12345_k12345" });
		assertEquals(flowExecutionKey, argumentExtractor.extractFlowExecutionKey(context));
	}
}