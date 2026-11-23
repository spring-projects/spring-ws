/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.xpath;

import java.util.Map;

class Jaxp13XPathExpressionFactoryTests extends AbstractXPathExpressionFactoryTests {

	@Override
	protected XPathExpression createXPathExpression(String expression) {
		return Jaxp13XPathExpressionFactory.createXPathExpression(expression);
	}

	@Override
	protected XPathExpression createXPathExpression(String expression, Map<String, String> namespaces) {
		return Jaxp13XPathExpressionFactory.createXPathExpression(expression, namespaces);
	}

}
