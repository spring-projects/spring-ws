/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.xpath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XPathExpressionFactoryBeanTests {

	private XPathExpressionFactoryBean factoryBean;

	@BeforeEach
	void setUp() {
		this.factoryBean = new XPathExpressionFactoryBean();
	}

	@Test
	void testFactoryBean() throws Exception {

		this.factoryBean.setExpression("/root");
		this.factoryBean.afterPropertiesSet();
		Object result = this.factoryBean.getObject();

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(XPathExpression.class);
		assertThat(this.factoryBean.isSingleton()).isTrue();
		assertThat(this.factoryBean.getObject()).isInstanceOf(XPathExpression.class);
	}

}
