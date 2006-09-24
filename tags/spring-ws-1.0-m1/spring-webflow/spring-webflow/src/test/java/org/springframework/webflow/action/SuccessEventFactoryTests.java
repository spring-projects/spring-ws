package org.springframework.webflow.action;

import junit.framework.TestCase;

import org.springframework.webflow.Event;
import org.springframework.webflow.test.MockRequestContext;

public class SuccessEventFactoryTests extends TestCase {

	private MockRequestContext context = new MockRequestContext();

	private SuccessEventFactory factory = new SuccessEventFactory();

	public void testDefaultAdaptionRules() {
		Event result = factory.createResultEvent(this, "result", context);
		assertEquals("success", result.getId());
		assertEquals("result", result.getAttributes().getString("result"));
	}
}