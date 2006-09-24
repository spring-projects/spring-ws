package org.springframework.webflow.support;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.springframework.webflow.ScopeType;
import org.springframework.webflow.test.MockRequestContext;

public class SimpleFlowVariableTests extends TestCase {
	private MockRequestContext context = new MockRequestContext();

	public void testCreateValidFlowVariable() {
		SimpleFlowVariable variable = new SimpleFlowVariable("var", ArrayList.class);
		variable.create(context);
		assertTrue(context.getFlowScope().contains("var"));
		context.getFlowScope().getRequired("var", ArrayList.class);
	}
	
	public void testCreateValidFlowVariableCustomScope() {
		SimpleFlowVariable variable = new SimpleFlowVariable("var", ArrayList.class, ScopeType.REQUEST);
		variable.create(context);
		assertTrue(context.getRequestScope().contains("var"));
		context.getRequestScope().getRequired("var", ArrayList.class);
	}
	
	public void testCreateVariableNoDefaultConstructor() {
		SimpleFlowVariable variable = new SimpleFlowVariable("var", Integer.class);
		try {
			variable.create(context);
			fail("should have failed");
		} catch (Exception e) {
			
		}
	}
}
