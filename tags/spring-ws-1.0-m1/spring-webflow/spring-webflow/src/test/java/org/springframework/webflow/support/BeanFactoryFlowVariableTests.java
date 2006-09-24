package org.springframework.webflow.support;

import junit.framework.TestCase;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.webflow.test.MockRequestContext;

public class BeanFactoryFlowVariableTests extends TestCase {
	private MockRequestContext context = new MockRequestContext();

	public void testCreateValidFlowVariable() {
		StaticApplicationContext beanFactory = new StaticApplicationContext();
		beanFactory.registerPrototype("bean", Object.class);
		BeanFactoryFlowVariable variable = new BeanFactoryFlowVariable("var", "bean", beanFactory);
		variable.create(context);
		context.getFlowScope().getRequired("var");
	}
}