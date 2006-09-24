package org.springframework.webflow.support;

import junit.framework.TestCase;

import org.springframework.binding.expression.ExpressionParser;

public class DefaultExpressionParserFactoryTests extends TestCase {
	public void testGetDefaultExpressionParser() {
		DefaultExpressionParserFactory f = new DefaultExpressionParserFactory();
		ExpressionParser parser = f.getExpressionParser();
		assertNotNull(parser);
		assertTrue(parser instanceof WebFlowOgnlExpressionParser);
		assertSame(parser, f.getExpressionParser());
	}
}
