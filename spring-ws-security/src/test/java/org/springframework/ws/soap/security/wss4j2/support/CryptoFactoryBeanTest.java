/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.security.wss4j2.support;

import static org.assertj.core.api.Assertions.*;

import java.util.Properties;

import org.apache.wss4j.common.crypto.Merlin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

public class CryptoFactoryBeanTest {

	private CryptoFactoryBean factoryBean;

	@BeforeEach
	public void setUp() {
		factoryBean = new CryptoFactoryBean();
	}

	@Test
	public void testSetConfiguration() throws Exception {

		Properties configuration = new Properties();
		configuration.setProperty("org.apache.ws.security.crypto.provider",
				"org.apache.ws.security.components.crypto.Merlin");
		configuration.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", "jceks");
		configuration.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", "123456");
		configuration.setProperty("org.apache.ws.security.crypto.merlin.file", "private.jks");

		factoryBean.setConfiguration(configuration);
		factoryBean.afterPropertiesSet();

		Object result = factoryBean.getObject();

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Merlin.class);
	}

	@Test
	public void testProperties() throws Exception {

		factoryBean.setKeyStoreType("jceks");
		factoryBean.setKeyStorePassword("123456");
		factoryBean.setKeyStoreLocation(new ClassPathResource("private.jks"));
		factoryBean.afterPropertiesSet();
		Object result = factoryBean.getObject();

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Merlin.class);
	}
}
