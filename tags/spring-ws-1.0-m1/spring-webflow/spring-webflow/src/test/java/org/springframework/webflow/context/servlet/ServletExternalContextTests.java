package org.springframework.webflow.context.servlet;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

public class ServletExternalContextTests extends TestCase {
	private ServletExternalContext context = new ServletExternalContext(new MockServletContext(),
			new MockHttpServletRequest(), new MockHttpServletResponse());
	
	public void testApplicationMap() {
		assertEquals(1, context.getApplicationMap().size());
		context.getApplicationMap().put("foo", "bar");
		assertEquals("bar", context.getApplicationMap().get("foo"));
		assertEquals("bar", context.getContext().getAttribute("foo"));
	}

	public void testSessionMap() {
		assertEquals(0, context.getSessionMap().size());
		context.getSessionMap().put("foo", "bar");
		assertEquals("bar", context.getSessionMap().get("foo"));
		assertEquals("bar", context.getRequest().getSession().getAttribute("foo"));
	}

	public void testRequestMap() {
		assertEquals(0, context.getRequestMap().size());
		context.getRequestMap().put("foo", "bar");
		assertEquals("bar", context.getRequestMap().get("foo"));
		assertEquals("bar", context.getRequest().getAttribute("foo"));
	}
	
	public void testOther() {
		assertEquals(null, context.getRequestPathInfo());
		assertEquals("", context.getDispatcherPath());
		assertNotNull(context.getResponse());
	}
}
