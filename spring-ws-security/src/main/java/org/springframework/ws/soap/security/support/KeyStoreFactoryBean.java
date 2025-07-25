/*
 * Copyright 2005-present the original author or authors.
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

package org.springframework.ws.soap.security.support;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Spring factory bean for a {@link KeyStore}.
 * <p>
 * To load an existing key store, you must set the {@code location} property. If this
 * property is not set, a new, empty key store is created, which is most likely not what
 * you want.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see #setLocation(org.springframework.core.io.Resource)
 * @see KeyStore
 */
public class KeyStoreFactoryBean implements FactoryBean<KeyStore>, InitializingBean {

	private static final Log logger = LogFactory.getLog(KeyStoreFactoryBean.class);

	private @Nullable KeyStore keyStore;

	private @Nullable String type;

	private @Nullable String provider;

	private @Nullable Resource location;

	private char @Nullable [] password;

	/**
	 * Sets the location of the key store to use. If this is not set, a new, empty key
	 * store will be used.
	 * @see KeyStore#load(java.io.InputStream,char[])
	 */
	public void setLocation(@Nullable Resource location) {
		this.location = location;
	}

	/**
	 * Sets the password to use for integrity checking. If this property is not set, then
	 * integrity checking is not performed.
	 */
	public void setPassword(@Nullable String password) {
		if (password != null) {
			this.password = password.toCharArray();
		}
	}

	/**
	 * Sets the provider of the key store to use. If this is not set, the default is used.
	 */
	public void setProvider(String provider) {
		this.provider = provider;
	}

	/**
	 * Sets the type of the {@code KeyStore} to use. If this is not set, the default is
	 * used.
	 * @see KeyStore#getDefaultType()
	 */
	public void setType(@Nullable String type) {
		this.type = type;
	}

	@Override
	public KeyStore getObject() {
		Assert.state(this.keyStore != null, "KeyStore has not been initialized");
		return this.keyStore;
	}

	@Override
	public Class<KeyStore> getObjectType() {
		return KeyStore.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public final void afterPropertiesSet() throws GeneralSecurityException, IOException {
		if (StringUtils.hasLength(this.provider) && StringUtils.hasLength(this.type)) {
			this.keyStore = KeyStore.getInstance(this.type, this.provider);
		}
		else if (StringUtils.hasLength(this.type)) {
			this.keyStore = KeyStore.getInstance(this.type);
		}
		else {
			this.keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		}
		InputStream is = null;
		try {
			if (this.location != null && this.location.exists()) {
				is = this.location.getInputStream();
				if (logger.isInfoEnabled()) {
					logger.info("Loading key store from " + this.location);
				}
			}
			else if (logger.isWarnEnabled()) {
				logger.warn("Creating empty key store");
			}
			this.keyStore.load(is, this.password);
		}
		finally {
			if (is != null) {
				is.close();
			}
		}
	}

}
