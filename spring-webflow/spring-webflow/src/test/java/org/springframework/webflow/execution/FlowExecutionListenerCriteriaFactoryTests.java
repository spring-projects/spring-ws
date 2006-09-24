package org.springframework.webflow.execution;

import junit.framework.TestCase;

import org.springframework.webflow.Flow;

public class FlowExecutionListenerCriteriaFactoryTests extends TestCase {

	private FlowExecutionListenerCriteriaFactory factory = new FlowExecutionListenerCriteriaFactory();

	public void testAllFlows() {
		FlowExecutionListenerCriteria c = factory.allFlows();
		assertEquals(true, c.appliesTo(new Flow("foo")));
	}

	public void testFlowMatch() {
		FlowExecutionListenerCriteria c = factory.flow("foo");
		assertEquals(true, c.appliesTo(new Flow("foo")));
		assertEquals(false, c.appliesTo(new Flow("baz")));
	}

	public void testMultipleFlowMatch() {
		FlowExecutionListenerCriteria c = factory.flows(new String[] { "foo", "bar" });
		assertEquals(true, c.appliesTo(new Flow("foo")));
		assertEquals(true, c.appliesTo(new Flow("bar")));
		assertEquals(false, c.appliesTo(new Flow("baz")));
	}
}
