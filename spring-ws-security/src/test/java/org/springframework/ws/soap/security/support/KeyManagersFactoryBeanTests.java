/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.security.support;

import javax.net.ssl.KeyManager;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeyManagersFactoryBeanTests {

	@Test
	void defaults() throws Exception {

		KeyManagersFactoryBean factoryBean = new KeyManagersFactoryBean();
		factoryBean.afterPropertiesSet();
		KeyManager[] keyManagers = factoryBean.getObject();

		assertThat(keyManagers).isNotNull();
		assertThat(keyManagers).hasSize(1);
	}

	@Test
	void algorithm() throws Exception {

		KeyManagersFactoryBean factoryBean = new KeyManagersFactoryBean();
		factoryBean.setAlgorithm("PKIX");
		factoryBean.afterPropertiesSet();
		KeyManager[] keyManagers = factoryBean.getObject();

		assertThat(keyManagers).isNotNull();
		assertThat(keyManagers).hasSize(1);
	}

}
