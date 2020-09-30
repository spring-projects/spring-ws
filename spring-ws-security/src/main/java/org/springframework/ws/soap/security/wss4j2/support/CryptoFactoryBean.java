/*
 * Copyright 2005-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.security.wss4j2.support;

import java.io.IOException;
import java.util.Properties;

import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.apache.wss4j.common.crypto.Merlin;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * Spring factory bean for a WSS4J {@link Crypto}. Allows for strong-typed property configuration, or configuration
 * through {@link Properties}.
 * <p>
 * Requires either individual properties, or the {@link #setConfiguration(java.util.Properties) configuration} property
 * to be set.
 *
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 * @author Jamin Hitchcock
 * @see org.apache.ws.security.components.crypto.Crypto
 * @since 2.3.0
 */
public class CryptoFactoryBean implements FactoryBean<Crypto>, InitializingBean {

	private Properties configuration = new Properties();

	private Crypto crypto;

	private static final String CRYPTO_PROVIDER_PROPERTY = "org.apache.wss4j.crypto.provider";

	/**
	 * Sets the configuration of the Crypto. Setting this property overrides all previously set configuration, through the
	 * type-safe properties
	 *
	 * @see org.apache.ws.security.components.crypto.CryptoFactory#getInstance(java.util.Properties)
	 */
	public void setConfiguration(Properties properties) {
		Assert.notNull(properties, "'properties' must not be null");
		this.configuration.putAll(properties);
	}

	/**
	 * Sets the {@link org.apache.ws.security.components.crypto.Crypto} provider name. Defaults to
	 * {@link org.apache.ws.security.components.crypto.Merlin}.
	 * <p>
	 * This property maps to the WSS4J {@code org.apache.ws.security.crypto.provider} property.
	 *
	 * @param cryptoProviderClass the crypto provider class
	 */
	public void setCryptoProvider(Class<? extends Crypto> cryptoProviderClass) {
		this.configuration.setProperty(CRYPTO_PROVIDER_PROPERTY, cryptoProviderClass.getName());
	}

	/**
	 * Sets the location of the key store to be loaded in the {@link org.apache.ws.security.components.crypto.Crypto}
	 * instance.
	 * <p>
	 * This property maps to the WSS4J {@code org.apache.ws.security.crypto.merlin.file} property.
	 *
	 * @param location the key store location
	 * @throws java.io.IOException when the resource cannot be opened
	 */
	public void setKeyStoreLocation(Resource location) throws IOException {
		String resourcePath = getResourcePath(location);
		this.configuration.setProperty("org.apache.ws.security.crypto.merlin.file", resourcePath);
	}

	private String getResourcePath(Resource resource) throws IOException {
		try {
			return resource.getFile().getAbsolutePath();
		} catch (IOException ex) {
			if (resource instanceof ClassPathResource) {
				ClassPathResource classPathResource = (ClassPathResource) resource;
				return classPathResource.getPath();
			} else {
				throw ex;
			}
		}
	}

	/**
	 * Sets the key store provider.
	 * <p>
	 * This property maps to the WSS4J {@code org.apache.ws.security.crypto.merlin.keystore.provider} property.
	 *
	 * @param provider the key store provider
	 */
	public void setKeyStoreProvider(String provider) {
		this.configuration.setProperty("org.apache.ws.security.crypto.merlin.keystore.provider", provider);
	}

	/**
	 * Sets the key store password. Defaults to {@code security}.
	 * <p>
	 * This property maps to the WSS4J {@code org.apache.ws.security.crypto.merlin.keystore.password} property.
	 *
	 * @param password the key store password
	 */
	public void setKeyStorePassword(String password) {
		this.configuration.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", password);
	}

	/**
	 * Sets the key store type. Defaults to {@link java.security.KeyStore#getDefaultType()}.
	 * <p>
	 * This property maps to the WSS4J {@code org.apache.ws.security.crypto.merlin.keystore.type} property.
	 *
	 * @param type the key store type
	 */
	public void setKeyStoreType(String type) {
		this.configuration.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", type);
	}

	/**
	 * Sets the trust store password. Defaults to {@code changeit}.
	 * <p>
	 * WSS4J crypto uses the standard J2SE trust store, i.e. {@code $JAVA_HOME/lib/security/cacerts}.
	 * <p>
	 * This property maps to the WSS4J {@code org.apache.ws.security.crypto.merlin.cacerts.password} property.
	 *
	 * @param password the trust store password
	 */
	public void setTrustStorePassword(String password) {
		this.configuration.setProperty("org.apache.ws.security.crypto.merlin.cacerts.password", password);
	}

	/**
	 * Sets the alias name of the default certificate which has been specified as a property. This should be the
	 * certificate that is used for signature and encryption. This alias corresponds to the certificate that should be
	 * used whenever KeyInfo is not present in a signed or an encrypted message.
	 * <p>
	 * This property maps to the WSS4J {@code org.apache.ws.security.crypto.merlin.keystore.alias} property.
	 *
	 * @param defaultX509Alias alias name of the default X509 certificate
	 */
	public void setDefaultX509Alias(String defaultX509Alias) {
		this.configuration.setProperty("org.apache.ws.security.crypto.merlin.keystore.alias", defaultX509Alias);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (!configuration.containsKey(CRYPTO_PROVIDER_PROPERTY)) {
			configuration.setProperty(CRYPTO_PROVIDER_PROPERTY, Merlin.class.getName());
		}
		this.crypto = CryptoFactory.getInstance(configuration);
	}

	@Override
	public Class<Crypto> getObjectType() {
		return Crypto.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public Crypto getObject() throws Exception {
		return crypto;
	}

}
