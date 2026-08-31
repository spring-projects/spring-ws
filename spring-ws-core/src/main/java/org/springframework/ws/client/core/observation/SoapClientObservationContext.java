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

import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.transport.HeadersAwareSenderWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Context that holds information for metadata collection during the
 * {@link SoapClientObservationDocumentation#SOAP_CLIENT_REQUESTS SOAP client}
 * observations.
 * <p>
 * This context also extends {@link RequestReplySenderContext} for propagating tracing
 * information over supported transports. Transports that do not implement
 * {@link HeadersAwareSenderWebServiceConnection} silently opt out of propagation.
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

	/**
	 * Create a context for the exchange described by the given message context, to be
	 * sent over the given connection. The context registers itself with the message
	 * context so that it can be retrieved using
	 * {@link #findCurrentObservationContext(MessageContext)}.
	 * @param messageContext the message context of the current request
	 * @param connection the connection used to send the request
	 */
	public SoapClientObservationContext(MessageContext messageContext, WebServiceConnection connection) {
		super(SETTER);
		this.messageContext = messageContext;
		setCarrier(connection);
		messageContext.setProperty(CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE, this);
	}

	/**
	 * Return the context of the observation that the given message context takes part in,
	 * if any. This allows a
	 * {@link org.springframework.ws.client.support.interceptor.ClientInterceptor
	 * ClientInterceptor} to contribute additional metadata to the current observation.
	 * @param messageContext the message context of the current request
	 * @return the current observation context, or {@link Optional#empty()} if the request
	 * is not being observed
	 */
	public static Optional<SoapClientObservationContext> findCurrentObservationContext(MessageContext messageContext) {
		return Optional.ofNullable(
				(SoapClientObservationContext) messageContext.getProperty(CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE));
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
		return this.messageContext.hasResponse() ? this.messageContext.getResponse() : null;
	}

	/**
	 * Return the target namespace of the operation, or {@code null} if it could not be
	 * determined.
	 */
	public @Nullable String getNamespace() {
		return this.namespace;
	}

	/**
	 * Set the target namespace of the operation.
	 * @param namespace the target namespace, or {@code null} if unknown
	 */
	public void setNamespace(@Nullable String namespace) {
		this.namespace = namespace;
	}

	/**
	 * Return the name of the operation, or {@code null} if it could not be determined.
	 */
	public @Nullable String getOperationName() {
		return this.operationName;
	}

	/**
	 * Set the name of the operation.
	 * @param operationName the name of the operation, or {@code null} if unknown
	 */
	public void setOperationName(@Nullable String operationName) {
		this.operationName = operationName;
	}

	/**
	 * Set the {@link #getNamespace() namespace} and {@link #getOperationName() operation
	 * name} from the qualified name of the root element of the request payload. Does
	 * nothing if the given qualified name is {@code null}.
	 * @param payloadRootQName the qualified name of the payload root element
	 */
	public void setPayloadRootQName(@Nullable QName payloadRootQName) {
		if (payloadRootQName != null) {
			String namespace = payloadRootQName.getNamespaceURI();
			setNamespace(StringUtils.hasText(namespace) ? namespace : null);
			setOperationName(payloadRootQName.getLocalPart());
		}
	}

	/**
	 * Return the SOAP action of the request, or {@code null} if the request is not a
	 * {@link SoapMessage} or does not define one.
	 */
	public @Nullable String getSoapAction() {
		WebServiceMessage request = this.messageContext.getRequest();
		if (request instanceof SoapMessage soapMessage) {
			return soapMessage.getSoapAction();
		}
		return null;
	}

	/**
	 * Return the URI that the request is sent to, or {@code null} if the connection does
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
	 * Writes tracing information as request headers, for transports that support them.
	 */
	static final class HeaderSetter implements Propagator.Setter<WebServiceConnection> {

		@Override
		public void set(@Nullable WebServiceConnection connection, String key, String value) {
			if (connection instanceof HeadersAwareSenderWebServiceConnection wsConnection) {
				try {
					wsConnection.addRequestHeader(key, value);
				}
				catch (IOException ex) {
					// Tracing information is best effort, ignore
				}
			}
		}

	}

}
