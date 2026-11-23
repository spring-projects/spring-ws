/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.config;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.xml.xsd.commons.CommonsXsdSchemaCollection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arjen Poutsma
 */
class WsdlBeanDefinitionParserTests {

	private ApplicationContext applicationContext;

	@BeforeEach
	void setUp() {
		this.applicationContext = new ClassPathXmlApplicationContext("wsdlBeanDefinitionParserTest.xml", getClass());
	}

	@Test
	void staticWsdl() {

		Map<String, SimpleWsdl11Definition> result = this.applicationContext
			.getBeansOfType(SimpleWsdl11Definition.class);

		assertThat(result).isNotEmpty();

		String beanName = result.keySet().iterator().next();

		assertThat(beanName).isEqualTo("simple");
	}

	@Test
	void dynamicWsdl() {

		Map<String, ?> result = this.applicationContext.getBeansOfType(DefaultWsdl11Definition.class);

		assertThat(result).isNotEmpty();

		result = this.applicationContext.getBeansOfType(CommonsXsdSchemaCollection.class);

		assertThat(result).isNotEmpty();
	}

}
