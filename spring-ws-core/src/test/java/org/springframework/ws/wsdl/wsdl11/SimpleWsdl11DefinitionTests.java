/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.wsdl.wsdl11;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleWsdl11DefinitionTests {

	private SimpleWsdl11Definition definition;

	@BeforeEach
	void setUp() throws Exception {

		this.definition = new SimpleWsdl11Definition();
		this.definition.setWsdl(new ClassPathResource("complete.wsdl", getClass()));
		this.definition.afterPropertiesSet();
	}

	@Test
	void testGetSource() {
		assertThat(this.definition.getSource()).isNotNull();
	}

}
