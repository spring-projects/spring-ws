package org.springframework.webflow.support;

import junit.framework.TestCase;

import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.webflow.Event;
import org.springframework.webflow.test.MockRequestContext;

public class BooleanExpressionTransitionCriteriaTests extends TestCase {
	ExpressionParser parser = new DefaultExpressionParserFactory().getExpressionParser();

	public void testMatchCriteria() {
		Expression exp = parser.parseExpression("${requestScope.flag}");
		BooleanExpressionTransitionCriteria c = new BooleanExpressionTransitionCriteria(exp);
		MockRequestContext context = new MockRequestContext();
		context.getRequestScope().put("flag", Boolean.TRUE);
		assertEquals(true, c.test(context));
	}	

	public void testNotABoolean() {
		Expression exp = parser.parseExpression("${requestScope.flag}");
		BooleanExpressionTransitionCriteria c = new BooleanExpressionTransitionCriteria(exp);
		MockRequestContext context = new MockRequestContext();
		context.getRequestScope().put("flag", "foo");
		try {
			c.test(context);
			fail("not a boolean");
		} catch (IllegalArgumentException e) {
			
		}
	}
	
	public void testResult() {
		Expression exp = parser.parseExpression("${#result == 'foo'}");
		BooleanExpressionTransitionCriteria c = new BooleanExpressionTransitionCriteria(exp);
		MockRequestContext context = new MockRequestContext();
		context.setLastEvent(new Event(this, "foo"));
		assertEquals(true, c.test(context));
	}
}