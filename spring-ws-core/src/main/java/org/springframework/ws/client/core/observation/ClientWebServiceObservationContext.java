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

package org.springframework.ws.client.core.observation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import io.micrometer.observation.transport.RequestReplySenderContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.HeadersAwareSenderWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Context that holds information for metadata collection during the
 * {@link ClientWebServiceObservationDocumentation#WEB_SERVICE_CLIENT_EXCHANGES web
 * service clientPexchanges} observations.
 *
 * @author Stephane Nicoll
 * @since 6.1.0
 */
public class ClientWebServiceObservationContext
		extends RequestReplySenderContext<WebServiceConnection, WebServiceMessage> {

	private static final Log logger = LogFactory.getLog(ClientWebServiceObservationContext.class);

	public ClientWebServiceObservationContext(WebServiceConnection connection) {
		super(ClientWebServiceObservationContext::setRequestHeader);
		setCarrier(connection);
	}

	private static void setRequestHeader(@Nullable WebServiceConnection connection, String name, String value) {
		if (connection instanceof HeadersAwareSenderWebServiceConnection headersAwareConnection) {
			try {
				headersAwareConnection.addRequestHeader(name, value);
			}
			catch (IOException ex) {
				logger.warn("Failed to add request header '" + name + "'", ex);
			}
		}
	}

	public @Nullable URI getUri() {
		if (getCarrier() != null) {
			try {
				return getCarrier().getUri();
			}
			catch (URISyntaxException ex) {
				// ignore
			}
		}
		return null;
	}

}
