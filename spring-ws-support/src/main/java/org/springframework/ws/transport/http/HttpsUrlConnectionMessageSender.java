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

package org.springframework.ws.transport.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Extension of {@link HttpUrlConnectionMessageSender} that adds support for (self-signed) HTTPS certificates.
 *
 * @author Alex Marshall
 * @author Arjen Poutsma
 * @since 1.5.8
 */
public class HttpsUrlConnectionMessageSender extends HttpUrlConnectionMessageSender implements InitializingBean {

	/** The default SSL protocol. */
	public static final String DEFAULT_SSL_PROTOCOL = "ssl";

	private String sslProtocol = DEFAULT_SSL_PROTOCOL;

	private String sslProvider;

	private KeyManager[] keyManagers;

	private TrustManager[] trustManagers;

	private HostnameVerifier hostnameVerifier;

	private SecureRandom rnd;

	private SSLSocketFactory sslSocketFactory;

	/**
	 * Sets the SSL protocol to use. Default is {@code ssl}.
	 *
	 * @see SSLContext#getInstance(String, String)
	 */
	public void setSslProtocol(String sslProtocol) {
		Assert.hasLength(sslProtocol, "'sslProtocol' must not be empty");
		this.sslProtocol = sslProtocol;
	}

	/**
	 * Sets the SSL provider to use. Default is empty, to use the default provider.
	 *
	 * @see SSLContext#getInstance(String, String)
	 */
	public void setSslProvider(String sslProvider) {
		this.sslProvider = sslProvider;
	}

	/**
	 * Specifies the key managers to use for this message sender.
	 * <p>
	 * Setting either this property or {@link #setTrustManagers(TrustManager[]) trustManagers} is required.
	 *
	 * @see SSLContext#init(KeyManager[], TrustManager[], SecureRandom)
	 */
	public void setKeyManagers(KeyManager[] keyManagers) {
		this.keyManagers = keyManagers;
	}

	/**
	 * Specifies the trust managers to use for this message sender.
	 * <p>
	 * Setting either this property or {@link #setKeyManagers(KeyManager[]) keyManagers} is required.
	 *
	 * @see SSLContext#init(KeyManager[], TrustManager[], SecureRandom)
	 */
	public void setTrustManagers(TrustManager[] trustManagers) {
		this.trustManagers = trustManagers;
	}

	/**
	 * Specifies the host name verifier to use for this message sender.
	 *
	 * @see HttpsURLConnection#setHostnameVerifier(HostnameVerifier)
	 */
	public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
		this.hostnameVerifier = hostnameVerifier;
	}

	/**
	 * Specifies the secure random to use for this message sender.
	 *
	 * @see SSLContext#init(KeyManager[], TrustManager[], SecureRandom)
	 */
	public void setSecureRandom(SecureRandom rnd) {
		this.rnd = rnd;
	}

	/**
	 * Specifies the SSLSocketFactory to use for this message sender.
	 *
	 * @see HttpsURLConnection#setSSLSocketFactory(SSLSocketFactory sf)
	 */
	public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
		this.sslSocketFactory = sslSocketFactory;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.isTrue(
				!(ObjectUtils.isEmpty(keyManagers) && ObjectUtils.isEmpty(trustManagers) && (sslSocketFactory == null)),
				"Setting either 'keyManagers', 'trustManagers' or 'sslSocketFactory' is required");
	}

	@Override
	protected void prepareConnection(HttpURLConnection connection) throws IOException {
		super.prepareConnection(connection);
		if (connection instanceof HttpsURLConnection) {
			HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
			httpsConnection.setSSLSocketFactory(createSslSocketFactory());

			if (hostnameVerifier != null) {
				httpsConnection.setHostnameVerifier(hostnameVerifier);
			}
		}
	}

	private SSLSocketFactory createSslSocketFactory() throws HttpsTransportException {
		if (this.sslSocketFactory != null) {
			return this.sslSocketFactory;
		}
		try {
			SSLContext sslContext = StringUtils.hasLength(sslProvider) ? SSLContext.getInstance(sslProtocol, sslProvider)
					: SSLContext.getInstance(sslProtocol);
			sslContext.init(keyManagers, trustManagers, rnd);
			if (logger.isDebugEnabled()) {
				logger.debug("Initialized SSL Context with key managers ["
						+ StringUtils.arrayToCommaDelimitedString(keyManagers) + "] trust managers ["
						+ StringUtils.arrayToCommaDelimitedString(trustManagers) + "] secure random [" + rnd + "]");
			}
			return sslContext.getSocketFactory();
		} catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
			throw new HttpsTransportException("Could not create SSLContext: " + ex.getMessage(), ex);
		} catch (KeyManagementException ex) {
			throw new HttpsTransportException("Could not initialize SSLContext: " + ex.getMessage(), ex);
		}

	}

}
