package org.springframework.webflow;

import junit.framework.TestCase;

import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.test.MockRequestContext;

public class AnnotedActionTests extends TestCase {

	private AnnotatedAction action = new AnnotatedAction(new TestAction());

	private MockRequestContext context = new MockRequestContext();

	protected void setUp() throws Exception {
	}

	public void testBasicExecute() throws Exception {
		assertEquals("success", action.execute(context).getId());
	}

	public void testExecuteWithCustomAttribute() throws Exception {
		action.getAttributeMap().put("attr", "value");
		action.setTargetAction(new AbstractAction() {
			protected Event doExecute(RequestContext context) throws Exception {
				assertEquals("value", context.getAttributes().getString("attr"));
				return success();
			}
		});
		assertEquals("success", action.execute(context).getId());
	}

	public void testExecuteWithName() throws Exception {
		action.getAttributeMap().put("name", "foo");
		action.setTargetAction(new AbstractAction() {
			protected Event doExecute(RequestContext context) throws Exception {
				assertEquals("foo", context.getAttributes().getString("name"));
				return success();
			}
		});
		assertEquals("foo.success", action.execute(context).getId());
	}
}