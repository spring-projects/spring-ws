/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.action;

import junit.framework.TestCase;

import org.springframework.binding.expression.Expression;
import org.springframework.binding.method.MethodSignature;
import org.springframework.binding.method.Parameter;
import org.springframework.binding.method.Parameters;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.Event;
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.support.WebFlowOgnlExpressionParser;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Unit test for the bean factory bean invoking action.
 * @author Keith Donald
 */
public class LocalBeanInvokingActionTests extends TestCase {

	private TestBean bean = new TestBean();

	private LocalBeanInvokingAction action;

	private MockRequestContext context = new MockRequestContext();

	public void setUp() {
		action = new LocalBeanInvokingAction(new MethodSignature("execute"), bean);
	}

	public void testInvokeBean() throws Exception {
		action.execute(context);
		assertTrue(bean.executed);
	}

	public void testNullTargetBean() throws Exception {
		try {
			action = new LocalBeanInvokingAction(new MethodSignature("execute"), null);
			fail("Should've failed with iae");
		}
		catch (IllegalArgumentException e) {

		}
	}

	public void testExposeResultInScopes() throws Exception {
		AttributeMap attributes = new AttributeMap();
		attributes.put("foo", "a string value");
		attributes.put("bar", "12345");
		context.setLastEvent(new Event(this, "submit", attributes));
		MethodSignature method = new MethodSignature("execute", new Parameters(new Parameter[] {
				new Parameter(String.class, expression("lastEvent.attributes.foo")),
				new Parameter(Integer.class, expression("lastEvent.attributes.bar")) }));
		action = new LocalBeanInvokingAction(method, bean);
		action.setResultSpecification(new ResultSpecification("foo", ScopeType.REQUEST));
		testInvokeBean();
		assertEquals(new Integer(12345), context.getRequestScope().get("foo"));

		context.getRequestScope().clear();

		action.setResultSpecification(new ResultSpecification("foo", ScopeType.FLOW));
		testInvokeBean();
		assertEquals(new Integer(12345), context.getFlowScope().get("foo"));
		assertNull(context.getRequestScope().get("foo"));
	}

	private Expression expression(String string) {
		return new WebFlowOgnlExpressionParser().parseExpression(string);
	}
}