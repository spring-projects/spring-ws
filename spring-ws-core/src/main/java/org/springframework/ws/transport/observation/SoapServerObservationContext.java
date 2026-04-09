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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerFactory;

import io.micrometer.observation.transport.Propagator;
import io.micrometer.observation.transport.RequestReplyReceiverContext;
import org.jspecify.annotations.Nullable;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.mapping.AbstractMethodEndpointMapping;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.support.PayloadRootUtils;
import org.springframework.ws.transport.HeadersAwareReceiverWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.http.HttpServletConnection;
import org.springframework.xml.transform.TransformerFactoryUtils;

/**
 * Context that holds information for metadata collection regarding
 * {@link SoapServerObservationDocumentation#SOAP_SERVER_REQUESTS SOAP requests}
 * observations.
 * <p>
 * This context also extends {@link RequestReplyReceiverContext} for propagating tracing
 * information over supported transports.
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

	private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactoryUtils.newInstance();

	private @Nullable MessageContext messageContext;

	private Supplier<Operation> operation = this::resolveOperation;

	public SoapServerObservationContext(WebServiceConnection connection) {
		super(GETTER);
		setCarrier(connection);
	}

	public static Optional<SoapServerObservationContext> findCurrentObservationContext(MessageContext holder) {
		return Optional
			.ofNullable((SoapServerObservationContext) holder.getProperty(CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE));
	}

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

	public @Nullable WebServiceMessage getRequest() {
		if (this.messageContext != null) {
			return this.messageContext.getRequest();
		}
		return null;
	}

	@Override
	public @Nullable WebServiceMessage getResponse() {
		if (this.messageContext != null && this.messageContext.hasResponse()) {
			return this.messageContext.getResponse();
		}
		return null;
	}

	public @Nullable String getNamespace() {
		return this.operation.get().namespace;
	}

	public @Nullable String getOperationName() {
		return this.operation.get().operationName;
	}

	public void setOperation(@Nullable String namespace, @Nullable String operationName) {
		this.operation = () -> new Operation(namespace, operationName);
	}

	private Operation resolveOperation() {
		if (this.messageContext != null) {
			Object lookupKey = this.messageContext.getProperty(AbstractMethodEndpointMapping.LOOKUP_KEY_PROPERTY);
			if (lookupKey instanceof QName qName) {
				return new Operation(qName.getNamespaceURI(), qName.getLocalPart());
			}
			else if (lookupKey instanceof String stringKey) {
				return new Operation(null, stringKey);
			}
			else {
				try {
					QName qName = PayloadRootUtils
						.getPayloadRootQName(this.messageContext.getRequest().getPayloadSource(), TRANSFORMER_FACTORY);
					if (qName != null) {
						return new Operation(qName.getNamespaceURI(), qName.getLocalPart());
					}
				}
				catch (Exception ex) {
					// ignore
				}
			}
		}
		return new Operation(null, null);
	}

	public @Nullable String getSoapAction() {
		WebServiceMessage request = getRequest();
		if (request instanceof SoapMessage soapMessage) {
			return soapMessage.getSoapAction();
		}
		return null;
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

	static final class HeaderGetter implements Propagator.Getter<WebServiceConnection> {

		@Override
		public @Nullable String get(WebServiceConnection connection, String key) {
			if (connection instanceof HeadersAwareReceiverWebServiceConnection wsConnection) {
				// skip HTTP transport as Servlet layer is already instrumented
				if (connection instanceof HttpServletConnection) {
					return null;
				}
				try {
					return wsConnection.getRequestHeaders(key).next();
				}
				catch (IOException exc) {
					return null;
				}
			}
			return null;
		}

	}

	record Operation(@Nullable String namespace, @Nullable String operationName) {
	}

}
