package org.springframework.webflow.support;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.webflow.test.MockRequestContext;

public class CollectionAddingPropertyExpressionTests extends TestCase {
	ExpressionParser parser = new DefaultExpressionParserFactory().getExpressionParser();

	public void testEvaluation() {
		Expression exp = parser.parseExpression("${requestScope.collection}");
		MockRequestContext context = new MockRequestContext();
		ArrayList list = new ArrayList();
		context.getRequestScope().put("collection", list);
		CollectionAddingPropertyExpression colExp = new CollectionAddingPropertyExpression(exp);
		assertSame(list, colExp.evaluateAgainst(context, null));
	}

	public void testAddToCollection() {
		Expression exp = parser.parseExpression("${requestScope.collection}");
		MockRequestContext context = new MockRequestContext();
		context.getRequestScope().put("collection", new ArrayList());
		CollectionAddingPropertyExpression colExp = new CollectionAddingPropertyExpression(exp);
		colExp.setValue(context, "1", null);
		colExp.setValue(context, "2", null);
		assertEquals("1", ((List)context.getRequestScope().getCollection("collection")).get(0));
		assertEquals("2", ((List)context.getRequestScope().getCollection("collection")).get(1));
	}
	
	public void testNotACollection() {
		Expression exp = parser.parseExpression("${requestScope.collection}");
		MockRequestContext context = new MockRequestContext();
		context.getRequestScope().put("collection", "bogus");
		CollectionAddingPropertyExpression colExp = new CollectionAddingPropertyExpression(exp);
		try {
			colExp.setValue(context, "1", null);
			fail("not a collection");
		} catch (IllegalArgumentException e) {
			
		}
	}
	
	public void testNoAddOnNullValue() {
		Expression exp = parser.parseExpression("${requestScope.collection}");
		MockRequestContext context = new MockRequestContext();
		context.getRequestScope().put("collection", new ArrayList());
		CollectionAddingPropertyExpression colExp = new CollectionAddingPropertyExpression(exp);
		colExp.setValue(context, null, null);
		colExp.setValue(context, "2", null);
		assertEquals("2", ((List)context.getRequestScope().getCollection("collection")).get(0));
	}
}