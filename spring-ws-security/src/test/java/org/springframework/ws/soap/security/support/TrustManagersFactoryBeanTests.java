/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.security.support;

import javax.net.ssl.TrustManager;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrustManagersFactoryBeanTests {

	@Test
	void defaults() throws Exception {

		TrustManagersFactoryBean factoryBean = new TrustManagersFactoryBean();
		factoryBean.afterPropertiesSet();
		TrustManager[] trustManagers = factoryBean.getObject();

		assertThat(trustManagers).isNotNull();
		assertThat(trustManagers).hasSize(1);
	}

	@Test
	void algorithm() throws Exception {

		TrustManagersFactoryBean factoryBean = new TrustManagersFactoryBean();
		factoryBean.setAlgorithm("PKIX");
		factoryBean.afterPropertiesSet();
		TrustManager[] trustManagers = factoryBean.getObject();

		assertThat(trustManagers).isNotNull();
		assertThat(trustManagers).hasSize(1);
	}

}
