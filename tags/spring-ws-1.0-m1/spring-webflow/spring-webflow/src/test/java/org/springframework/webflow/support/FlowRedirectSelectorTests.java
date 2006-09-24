package org.springframework.webflow.support;

import junit.framework.TestCase;

import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.test.MockRequestContext;

public class FlowRedirectSelectorTests extends TestCase {
	ExpressionParser parser = new DefaultExpressionParserFactory().getExpressionParser();

	public void testMakeSelection() {
		Expression exp = parser.parseExpression("${requestScope.flowIdVar}?a=b&c=${requestScope.bar}");
		FlowRedirectSelector selector = new FlowRedirectSelector(exp);
		MockRequestContext context = new MockRequestContext();
		context.getRequestScope().put("flowIdVar", "foo");
		context.getRequestScope().put("bar", "baz");
		ViewSelection selection = selector.makeSelection(context);
		assertTrue(selection instanceof FlowRedirect);
		FlowRedirect redirect = (FlowRedirect)selection;
		assertEquals("foo", redirect.getFlowId());
		assertEquals("b", redirect.getInput().get("a"));
		assertEquals("baz", redirect.getInput().get("c"));
	}
}