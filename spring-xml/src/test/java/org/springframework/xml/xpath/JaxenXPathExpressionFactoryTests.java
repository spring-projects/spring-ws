/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.xpath;

import java.util.Map;

class JaxenXPathExpressionFactoryTests extends AbstractXPathExpressionFactoryTests {

	@Override
	protected XPathExpression createXPathExpression(String expression) {
		return JaxenXPathExpressionFactory.createXPathExpression(expression);
	}

	@Override
	protected XPathExpression createXPathExpression(String expression, Map<String, String> namespaces) {
		return JaxenXPathExpressionFactory.createXPathExpression(expression, namespaces);
	}

	@Override
	public void testEvaluateAsDoubleNoNamespaces() {
		// Currently not working on Jaxen 1.1 beta 8, hence the override here
	}

	@Override
	public void testEvaluateAsDoubleNamespaces() {
		// Currently not working on Jaxen 1.1 beta 8, hence the override here
	}

}
