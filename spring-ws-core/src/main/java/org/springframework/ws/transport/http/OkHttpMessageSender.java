/*
 * Copyright 2005-2021 the original author or authors.
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

import java.net.URI;

import okhttp3.OkHttpClient;
import org.springframework.util.Assert;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * {@code WebServiceMessageSender} implementation that uses <a href="https://square.github.io/okhttp/">OkHttp</a>
 * to execute POST requests.
 * <p>
 * Allows to use a pre-configured OkHttpClient instance, potentially with authentication, interceptors, etc.
 *
 * @author Mathias Geat
 * @see OkHttpClient
 * @since 3.1.0
 */
public class OkHttpMessageSender extends AbstractHttpWebServiceMessageSender {

	private final OkHttpClient okHttpClient;

	public OkHttpMessageSender(OkHttpClient okHttpClient) {
		Assert.notNull(okHttpClient, "okHttpClient must not be null");
		this.okHttpClient = okHttpClient;
	}

	@Override
	public WebServiceConnection createConnection(URI uri) {
		return new OkHttpConnection(okHttpClient, uri);
	}
}
