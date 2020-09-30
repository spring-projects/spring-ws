/*
 * Copyright 2005-2018 the original author or authors.
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
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;

import org.springframework.ws.transport.WebServiceConnection;

/**
 * {@code WebServiceMessageSender} implementation that uses standard J2SE facilities to execute POST requests, without
 * support for HTTP authentication or advanced configuration options.
 * <p>
 * Consider {@link HttpComponentsMessageSender} for more sophisticated needs: this class is rather limited in its
 * capabilities.
 *
 * @author Arjen Poutsma
 * @author Kazuki Shimizu
 * @see java.net.HttpURLConnection
 * @since 1.0.0
 */
public class HttpUrlConnectionMessageSender extends AbstractHttpWebServiceMessageSender {

	private Duration connectionTimeout = Duration.ofSeconds(60);
	private Duration readTimeout = Duration.ofSeconds(60);

	/**
	 * Sets the timeout until a connection is established.
	 *
	 * @param connectTimeout the timeout value
	 * @see URLConnection#setConnectTimeout(int)
	 * @since 3.0.1
	 */
	public void setConnectionTimeout(Duration connectTimeout) {
		this.connectionTimeout = connectTimeout;
	}

	/**
	 * Set the socket read timeout.
	 *
	 * @param readTimeout the timeout value
	 * @see URLConnection#setReadTimeout(int)
	 * @since 3.0.1
	 */
	public void setReadTimeout(Duration readTimeout) {
		this.readTimeout = readTimeout;
	}

	@Override
	public WebServiceConnection createConnection(URI uri) throws IOException {
		URL url = uri.toURL();
		URLConnection connection = url.openConnection();
		if (!(connection instanceof HttpURLConnection)) {
			throw new HttpTransportException("URI [" + uri + "] is not an HTTP URL");
		} else {
			HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
			prepareConnection(httpURLConnection);
			return new HttpUrlConnection(httpURLConnection);
		}
	}

	/**
	 * Template method for preparing the given {@link java.net.HttpURLConnection}.
	 * <p>
	 * The default implementation prepares the connection for input and output, sets the HTTP method to POST, disables
	 * caching, and sets the {@code Accept-Encoding} header to gzip, if {@linkplain #setAcceptGzipEncoding(boolean)
	 * applicable}.
	 *
	 * @param connection the connection to prepare
	 * @throws IOException in case of I/O errors
	 */
	protected void prepareConnection(HttpURLConnection connection) throws IOException {
		connection.setRequestMethod(HttpTransportConstants.METHOD_POST);
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		if (isAcceptGzipEncoding()) {
			connection.setRequestProperty(HttpTransportConstants.HEADER_ACCEPT_ENCODING,
					HttpTransportConstants.CONTENT_ENCODING_GZIP);
		}
		connection.setConnectTimeout(Math.toIntExact(this.connectionTimeout.toMillis()));
		connection.setReadTimeout(Math.toIntExact(this.readTimeout.toMillis()));
	}

}
