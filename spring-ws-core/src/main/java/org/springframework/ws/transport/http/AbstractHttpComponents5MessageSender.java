/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.http;

import java.io.IOException;
import java.net.URI;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Base {@link AbstractHttpWebServiceMessageSender} implementation that uses
 * <a href="http://hc.apache.org/httpcomponents-client">Apache HttpClient</a> to execute
 * POST requests.
 * <p>
 * To configure the underlying {@link HttpClient} consider using
 * {@link HttpComponents5MessageSender}. To take control on how the {@link HttpClient} is
 * built, use {@link SimpleHttpComponents5MessageSender}.
 *
 * @author Stephane Nicoll
 * @since 4.1.0
 * @see HttpComponents5MessageSender
 * @see SimpleHttpComponents5MessageSender
 */
public abstract class AbstractHttpComponents5MessageSender extends AbstractHttpWebServiceMessageSender
		implements DisposableBean {

	/**
	 * Return the {@code HttpClient} used by this message sender.
	 */
	public abstract HttpClient getHttpClient();

	@Override
	public WebServiceConnection createConnection(URI uri) throws IOException {
		HttpPost httpPost = new HttpPost(uri);
		if (isAcceptGzipEncoding()) {
			httpPost.addHeader(HttpTransportConstants.HEADER_ACCEPT_ENCODING,
					HttpTransportConstants.CONTENT_ENCODING_GZIP);
		}
		HttpHost httpHost = HttpHost.create(uri);
		HttpContext httpContext = createContext(uri);
		return new HttpComponents5Connection(getHttpClient(), httpHost, httpPost, httpContext);
	}

	@Override
	public void destroy() throws Exception {
		if (getHttpClient() instanceof CloseableHttpClient client) {
			client.close();
		}
	}

	/**
	 * Template method that allows for creation of an {@link HttpContext} for the given
	 * uri. Default implementation returns {@code null}.
	 * @param uri the URI to create the context for
	 * @return the context, or {@code null}
	 */
	protected @Nullable HttpContext createContext(URI uri) {
		return null;
	}

}
