package org.springframework.webflow.support;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class FlowRedirectTests extends TestCase {
	public void testConstructAndAccess() {
		Map input = new HashMap();
		input.put("name", "value");
		FlowRedirect redirect = new FlowRedirect("foo", input);
		assertEquals("foo", redirect.getFlowId());
		assertEquals(1, redirect.getInput().size());
		assertEquals("value", redirect.getInput().get("name"));
		try {
			redirect.getInput().put("foo", "bar");
		} catch (UnsupportedOperationException e) {
			
		}
	}
	
	public void testNullParams() {
		try {
			FlowRedirect redirect = new FlowRedirect(null, null);
			fail("was null");
		} catch (IllegalArgumentException e) {
			
		}

	}
	
	public void testMapLookup() {
		FlowRedirect redirect = new FlowRedirect("foo", null);
		Map map = new HashMap();
		map.put("redirect", redirect);
		assertSame(redirect, map.get("redirect"));
	}
}