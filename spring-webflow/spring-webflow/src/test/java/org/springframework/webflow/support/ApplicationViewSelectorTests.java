package org.springframework.webflow.support;

import junit.framework.TestCase;

import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.test.MockRequestContext;

public class ApplicationViewSelectorTests extends TestCase {
	ExpressionParser parser = new DefaultExpressionParserFactory().getExpressionParser();

	public void testMakeSelection() {
		Expression exp = parser.parseExpression("${requestScope.viewVar}");
		ApplicationViewSelector selector = new ApplicationViewSelector(exp);
		MockRequestContext context = new MockRequestContext();
		context.getRequestScope().put("viewVar", "view");
		context.getRequestScope().put("foo", "bar");
		context.getFlowScope().put("foo", "bar2");
		context.getFlowScope().put("foo2", "bar");
		context.getConversationScope().put("foo", "bar3");
		context.getConversationScope().put("foo3", "bar");
		ViewSelection selection = selector.makeSelection(context);
		assertTrue(selection instanceof ApplicationView);
		ApplicationView view = (ApplicationView)selection;
		assertEquals("view", view.getViewName());
		assertEquals("bar", view.getModel().get("foo"));
		assertEquals("bar", view.getModel().get("foo2"));
		assertEquals("bar", view.getModel().get("foo3"));
	}
	
	public void testMakeNullSelection() {
		ApplicationViewSelector selector = new ApplicationViewSelector(new StaticExpression(null));
		MockRequestContext context = new MockRequestContext();
		ViewSelection selection = selector.makeSelection(context);
		assertTrue(selection == ViewSelection.NULL_VIEW);
	}

	public void testMakeNullSelectionEmptyString() {
		ApplicationViewSelector selector = new ApplicationViewSelector(new StaticExpression(""));
		MockRequestContext context = new MockRequestContext();
		ViewSelection selection = selector.makeSelection(context);
		assertTrue(selection == ViewSelection.NULL_VIEW);
	}
}