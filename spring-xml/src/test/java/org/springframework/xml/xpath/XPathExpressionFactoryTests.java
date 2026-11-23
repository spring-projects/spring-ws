/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.xpath;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class XPathExpressionFactoryTests {

	@Test
	void testCreateXPathExpression() {

		XPathExpression expression = XPathExpressionFactory.createXPathExpression("/root");

		assertThat(expression).isNotNull();
	}

	@Test
	void testCreateEmptyXPathExpression() {
		assertThatIllegalArgumentException().isThrownBy(() -> XPathExpressionFactory.createXPathExpression(""));
	}

}
