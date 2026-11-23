/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
 * {@code WebServiceMessageSender} implementation that uses standard J2SE facilities to
 * execute POST requests, without support for HTTP authentication or advanced
 * configuration options.
 * <p>
 * Consider {@link HttpComponents5MessageSender} for more sophisticated needs: this class
 * is rather limited in its capabilities.
 *
 * @author Arjen Poutsma
 * @author Kazuki Shimizu
 * @since 1.0.0
 * @see java.net.HttpURLConnection
 */
public class HttpUrlConnectionMessageSender extends AbstractHttpWebServiceMessageSender {

	private Duration connectionTimeout = Duration.ofSeconds(60);

	private Duration readTimeout = Duration.ofSeconds(60);

	/**
	 * Sets the timeout until a connection is established.
	 * @param connectTimeout the timeout value
	 * @since 3.0.1
	 * @see URLConnection#setConnectTimeout(int)
	 */
	public void setConnectionTimeout(Duration connectTimeout) {
		this.connectionTimeout = connectTimeout;
	}

	/**
	 * Set the socket read timeout.
	 * @param readTimeout the timeout value
	 * @since 3.0.1
	 * @see URLConnection#setReadTimeout(int)
	 */
	public void setReadTimeout(Duration readTimeout) {
		this.readTimeout = readTimeout;
	}

	@Override
	public WebServiceConnection createConnection(URI uri) throws IOException {
		URL url = uri.toURL();
		URLConnection connection = url.openConnection();
		if (!(connection instanceof HttpURLConnection httpURLConnection)) {
			throw new HttpTransportException("URI [" + uri + "] is not an HTTP URL");
		}
		else {
			prepareConnection(httpURLConnection);
			return new HttpUrlConnection(httpURLConnection);
		}
	}

	/**
	 * Template method for preparing the given {@link java.net.HttpURLConnection}.
	 * <p>
	 * The default implementation prepares the connection for input and output, sets the
	 * HTTP method to POST, disables caching, and sets the {@code Accept-Encoding} header
	 * to gzip, if {@linkplain #setAcceptGzipEncoding(boolean) applicable}.
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
