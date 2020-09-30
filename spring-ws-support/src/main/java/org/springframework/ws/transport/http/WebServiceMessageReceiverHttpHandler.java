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

import org.springframework.ws.transport.support.SimpleWebServiceMessageReceiverObjectSupport;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * {@link HttpHandler} that can be used to handle incoming {@link HttpExchange} service requests. Designed for Sun's JRE
 * 1.6 HTTP server.
 * <p>
 * Requires a {@link org.springframework.ws.WebServiceMessageFactory} which is used to convert the incoming
 * {@link HttpExchange} into a {@link org.springframework.ws.WebServiceMessage}, and passes that to the
 * {@link org.springframework.ws.transport.WebServiceMessageReceiver}
 * {@link #setMessageReceiver(org.springframework.ws.transport.WebServiceMessageReceiver) registered}.
 *
 * @author Arjen Poutsma
 * @see org.springframework.remoting.support.SimpleHttpServerFactoryBean
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
			connection.setChunkedEncoding(chunkedEncoding);
			try {
				handleConnection(connection);
			} catch (Exception ex) {
				logger.error(ex);
			}
		} else {
			httpExchange.sendResponseHeaders(HttpTransportConstants.STATUS_METHOD_NOT_ALLOWED, -1);
			httpExchange.close();
		}
	}
}
