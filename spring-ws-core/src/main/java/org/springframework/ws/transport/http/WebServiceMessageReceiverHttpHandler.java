/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.http;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.springframework.ws.transport.support.SimpleWebServiceMessageReceiverObjectSupport;

/**
 * {@link HttpHandler} that can be used to handle incoming {@link HttpExchange} service
 * requests. Designed for Sun's JRE 1.6 HTTP server.
 * <p>
 * Requires a {@link org.springframework.ws.WebServiceMessageFactory} which is used to
 * convert the incoming {@link HttpExchange} into a
 * {@link org.springframework.ws.WebServiceMessage}, and passes that to the
 * {@link org.springframework.ws.transport.WebServiceMessageReceiver}
 * {@link #setMessageReceiver(org.springframework.ws.transport.WebServiceMessageReceiver)
 * registered}.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class WebServiceMessageReceiverHttpHandler extends SimpleWebServiceMessageReceiverObjectSupport
		implements HttpHandler {

	private boolean chunkedEncoding = false;

	/** Enables chunked encoding on response bodies. Defaults to {@code false}. */
	public void setChunkedEncoding(boolean chunkedEncoding) {
		this.chunkedEncoding = chunkedEncoding;
	}

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		if (HttpTransportConstants.METHOD_POST.equals(httpExchange.getRequestMethod())) {
			HttpExchangeConnection connection = new HttpExchangeConnection(httpExchange);
			connection.setChunkedEncoding(this.chunkedEncoding);
			try {
				handleConnection(connection);
			}
			catch (Exception ex) {
				this.logger.error(ex);
			}
		}
		else {
			httpExchange.sendResponseHeaders(HttpTransportConstants.STATUS_METHOD_NOT_ALLOWED, -1);
			httpExchange.close();
		}
	}

}
