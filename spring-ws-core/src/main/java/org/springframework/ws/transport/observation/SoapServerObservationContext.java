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

package org.springframework.ws.transport.observation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

import javax.xml.namespace.QName;

import io.micrometer.observation.transport.Propagator;
import io.micrometer.observation.transport.RequestReplyReceiverContext;
import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.mapping.AbstractEndpointMapping;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.transport.HeadersAwareReceiverWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.http.HttpServletConnection;

/**
 * Context that holds information for metadata collection regarding
 * {@link SoapServerObservationDocumentation#SOAP_SERVER_REQUESTS SOAP requests}
 * observations.
 * <p>
 * This context also extends {@link RequestReplyReceiverContext} for propagating tracing
 * information over supported transports. Tracing information is not extracted for the
 * HTTP transport, as the Servlet layer is instrumented already.
 *
 * @author Brian Clozel
 * @since 5.1.0
 */
public class SoapServerObservationContext extends RequestReplyReceiverContext<WebServiceConnection, WebServiceMessage> {

	/**
	 * Name of the message context property holding the
	 * {@link SoapServerObservationContext} for the current observation.
	 */
	private static final String CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE = SoapServerObservationContext.class.getName()
			+ ".context";

	private static final HeaderGetter GETTER = new HeaderGetter();

	private static final Operation UNKNOWN_OPERATION = new Operation(null, null);

	private @Nullable MessageContext messageContext;

	private @Nullable Operation operation;

	/**
	 * Create a context for a request received over the given connection. The
	 * {@link MessageContext} is not available until the request has been read, and must
	 * be provided using {@link #setAsCurrent(MessageContext)}.
	 * @param connection the connection the request was received on
	 */
	public SoapServerObservationContext(WebServiceConnection connection) {
		super(GETTER);
		setCarrier(connection);
	}

	/**
	 * Return the context of the observation that the given message context takes part in,
	 * if any. This allows an {@link org.springframework.ws.server.EndpointInterceptor
	 * EndpointInterceptor} to contribute additional metadata to the current observation.
	 * @param messageContext the message context of the current request
	 * @return the current observation context, or {@link Optional#empty()} if the request
	 * is not being observed
	 */
	public static Optional<SoapServerObservationContext> findCurrentObservationContext(MessageContext messageContext) {
		return Optional.ofNullable(
				(SoapServerObservationContext) messageContext.getProperty(CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE));
	}

	/**
	 * Associate this context with the given message context, and register it so that it
	 * can be retrieved using {@link #findCurrentObservationContext(MessageContext)}.
	 * @param messageContext the message context of the current request
	 */
	public void setAsCurrent(MessageContext messageContext) {
		messageContext.setProperty(CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE, this);
		this.messageContext = messageContext;
	}

	/**
	 * Return the {@link WebServiceConnection web service connection} used for the current
	 * request.
	 */
	public WebServiceConnection getConnection() {
		return Objects.requireNonNull(getCarrier());
	}

	/**
	 * Return the request, or {@code null} if it has not been read yet.
	 */
	public @Nullable WebServiceMessage getRequest() {
		return (this.messageContext != null) ? this.messageContext.getRequest() : null;
	}

	@Override
	public @Nullable WebServiceMessage getResponse() {
		if (this.messageContext != null && this.messageContext.hasResponse()) {
			return this.messageContext.getResponse();
		}
		return null;
	}

	/**
	 * Return the target namespace of the operation, or {@code null} if the request could
	 * not be mapped to an endpoint.
	 */
	public @Nullable String getNamespace() {
		return resolveOperation().namespace();
	}

	/**
	 * Return the name of the operation, or {@code null} if the request could not be
	 * mapped to an endpoint.
	 */
	public @Nullable String getOperationName() {
		return resolveOperation().operationName();
	}

	/**
	 * Set the target namespace and the name of the operation, overriding the values
	 * derived from the endpoint mapping.
	 * @param namespace the target namespace, or {@code null} if unknown
	 * @param operationName the name of the operation, or {@code null} if unknown
	 */
	public void setOperation(@Nullable String namespace, @Nullable String operationName) {
		this.operation = new Operation(namespace, operationName);
	}

	/**
	 * Resolve the operation once and cache the outcome, as the convention reads it
	 * several times.
	 * <p>
	 * The operation is derived from the key that the endpoint mapping used to look up the
	 * endpoint, and is therefore only available for requests that could be mapped. This
	 * deliberately avoids deriving it from the request payload, which is caller
	 * controlled and would let unmapped requests explode the cardinality of the
	 * {@code namespace} and {@code operation.name} key values.
	 */
	private Operation resolveOperation() {
		Operation operation = this.operation;
		if (operation != null) {
			return operation;
		}
		operation = (this.messageContext != null) ? fromLookupKey(this.messageContext) : UNKNOWN_OPERATION;
		// Only cache a successful resolution, as the endpoint mapping may not have run
		// yet
		if (operation != UNKNOWN_OPERATION) {
			this.operation = operation;
		}
		return operation;
	}

	private static Operation fromLookupKey(MessageContext messageContext) {
		Object lookupKey = messageContext.getProperty(AbstractEndpointMapping.LOOKUP_KEY_PROPERTY);
		if (lookupKey instanceof QName qName) {
			String namespace = qName.getNamespaceURI();
			return new Operation(StringUtils.hasText(namespace) ? namespace : null, qName.getLocalPart());
		}
		if (lookupKey instanceof String stringKey) {
			return new Operation(null, stringKey);
		}
		return UNKNOWN_OPERATION;
	}

	/**
	 * Return the SOAP action of the request, or {@code null} if the request is not a
	 * {@link SoapMessage} or does not define one.
	 */
	public @Nullable String getSoapAction() {
		WebServiceMessage request = getRequest();
		if (request instanceof SoapMessage soapMessage) {
			return soapMessage.getSoapAction();
		}
		return null;
	}

	/**
	 * Return the URI the request was received on, or {@code null} if the connection does
	 * not expose a valid URI.
	 */
	public @Nullable URI getUri() {
		try {
			return getConnection().getUri();
		}
		catch (URISyntaxException ex) {
			return null;
		}
	}

	/**
	 * Reads tracing information from the request headers, for transports that support
	 * them.
	 */
	static final class HeaderGetter implements Propagator.Getter<WebServiceConnection> {

		@Override
		public @Nullable String get(WebServiceConnection connection, String key) {
			// Skip the HTTP transport, as the Servlet layer is already instrumented
			if (connection instanceof HttpServletConnection
					|| !(connection instanceof HeadersAwareReceiverWebServiceConnection wsConnection)) {
				return null;
			}
			try {
				Iterator<String> values = wsConnection.getRequestHeaders(key);
				return values.hasNext() ? values.next() : null;
			}
			catch (IOException ex) {
				// Tracing information is best effort, ignore
				return null;
			}
		}

	}

	private record Operation(@Nullable String namespace, @Nullable String operationName) {
	}

}
