/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.security.wss4j2.support;

import java.util.Properties;

import org.apache.wss4j.common.crypto.Merlin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class CryptoFactoryBeanTests {

	private CryptoFactoryBean factoryBean;

	@BeforeEach
	void setUp() {
		this.factoryBean = new CryptoFactoryBean();
	}

	@Test
	void testSetConfiguration() throws Exception {

		Properties configuration = new Properties();
		configuration.setProperty("org.apache.ws.security.crypto.provider",
				"org.apache.ws.security.components.crypto.Merlin");
		configuration.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", "jceks");
		configuration.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", "123456");
		configuration.setProperty("org.apache.ws.security.crypto.merlin.file", "private.jks");

		this.factoryBean.setConfiguration(configuration);
		this.factoryBean.afterPropertiesSet();

		Object result = this.factoryBean.getObject();

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Merlin.class);
	}

	@Test
	void testProperties() throws Exception {

		this.factoryBean.setKeyStoreType("jceks");
		this.factoryBean.setKeyStorePassword("123456");
		this.factoryBean.setKeyStoreLocation(new ClassPathResource("private.jks"));
		this.factoryBean.afterPropertiesSet();
		Object result = this.factoryBean.getObject();

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Merlin.class);
	}

}
