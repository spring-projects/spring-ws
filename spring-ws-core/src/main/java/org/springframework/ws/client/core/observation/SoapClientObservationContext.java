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
import java.util.Objects;
import java.util.Optional;

import javax.xml.namespace.QName;

import io.micrometer.observation.transport.Propagator;
import io.micrometer.observation.transport.RequestReplySenderContext;
import org.jspecify.annotations.Nullable;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.HeadersAwareSenderWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Context that holds information for metadata collection during the
 * {@link SoapClientObservationDocumentation#SOAP_CLIENT_REQUESTS SOAP client}
 * observations.
 * <p>
 * This context also extends {@link RequestReplySenderContext} for propagating tracing
 * information over supported transports.
 *
 * @author Stephane Nicoll
 * @since 5.1.0
 */
public class SoapClientObservationContext extends RequestReplySenderContext<WebServiceConnection, WebServiceMessage> {

	/**
	 * Name of the message context property holding the
	 * {@link SoapClientObservationContext} for the current observation.
	 */
	private static final String CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE = SoapClientObservationContext.class.getName()
			+ ".context";

	private static final HeaderSetter SETTER = new HeaderSetter();

	private final MessageContext messageContext;

	private @Nullable String namespace;

	private @Nullable String operationName;

	public SoapClientObservationContext(MessageContext messageContext, WebServiceConnection connection) {
		super(SETTER);
		this.messageContext = messageContext;
		setCarrier(connection);
		messageContext.setProperty(CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE, this);
	}

	public static Optional<SoapClientObservationContext> findCurrentObservationContext(MessageContext holder) {
		return Optional
			.ofNullable((SoapClientObservationContext) holder.getProperty(CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE));
	}

	/**
	 * Return the {@link WebServiceConnection web service connection} used for the current
	 * request.
	 */
	public WebServiceConnection getConnection() {
		return Objects.requireNonNull(getCarrier());
	}

	/**
	 * Return the {@link MessageContext} used for the current request.
	 */
	public MessageContext getMessageContext() {
		return this.messageContext;
	}

	@Override
	public @Nullable WebServiceMessage getResponse() {
		if (this.messageContext.hasResponse()) {
			return this.messageContext.getResponse();
		}
		return null;
	}

	public @Nullable String getNamespace() {
		return this.namespace;
	}

	public void setNamespace(@Nullable String namespace) {
		this.namespace = namespace;
	}

	public @Nullable String getOperationName() {
		return this.operationName;
	}

	public void setOperationName(@Nullable String operationName) {
		this.operationName = operationName;
	}

	public void setPayloadRootQName(@Nullable QName payloadRootQName) {
		if (payloadRootQName != null) {
			setNamespace(payloadRootQName.getNamespaceURI());
			setOperationName(payloadRootQName.getLocalPart());
		}
	}

	public @Nullable URI getUri() {
		try {
			return getConnection().getUri();
		}
		catch (URISyntaxException ex) {
			// ignore
		}
		return null;
	}

	static final class HeaderSetter implements Propagator.Setter<WebServiceConnection> {

		@Override
		public void set(@Nullable WebServiceConnection connection, String key, String value) {
			if (connection instanceof HeadersAwareSenderWebServiceConnection wsConnection) {
				try {
					wsConnection.addRequestHeader(key, value);
				}
				catch (IOException ex) {
					// ignore
				}
			}
		}

	}

}
