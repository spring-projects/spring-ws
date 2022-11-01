/*
 * Copyright 2005-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.security.support;

import java.security.KeyStore;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * Spring factory bean for an array of {@link TrustManager}s.
 * <p>
 * Uses the {@link TrustManagerFactory} to create the {@code TrustManager}s.
 *
 * @author Arjen Poutsma
 * @see TrustManager
 * @see TrustManagerFactory
 * @since 2.2
 */
public class TrustManagersFactoryBean implements FactoryBean<TrustManager[]>, InitializingBean {

	private TrustManager[] trustManagers;

	private KeyStore keyStore;

	private String algorithm;

	private String provider;

	/**
	 * Sets the provider of the trust manager to use. If this is not set, the default is used.
	 */
	public void setProvider(String provider) {
		this.provider = provider;
	}

	/**
	 * Sets the algorithm of the {@code TrustManager} to use. If this is not set, the default is used.
	 *
	 * @see TrustManagerFactory#getDefaultAlgorithm()
	 */
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * Sets the source of certificate authorities and related trust material.
	 *
	 * @see TrustManagerFactory#init(KeyStore)
	 */
	public void setKeyStore(KeyStore keyStore) {
		this.keyStore = keyStore;
	}

	@Override
	public TrustManager[] getObject() throws Exception {
		return trustManagers;
	}

	@Override
	public Class<?> getObjectType() {
		return TrustManager[].class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		String algorithm = StringUtils.hasLength(this.algorithm) ? this.algorithm
				: TrustManagerFactory.getDefaultAlgorithm();

		TrustManagerFactory trustManagerFactory = StringUtils.hasLength(this.provider)
				? TrustManagerFactory.getInstance(algorithm, this.provider)
				: TrustManagerFactory.getInstance(algorithm);

		trustManagerFactory.init(keyStore);

		this.trustManagers = trustManagerFactory.getTrustManagers();
	}
}
