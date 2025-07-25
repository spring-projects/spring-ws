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

package org.springframework.ws.client.core.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.destination.DestinationProvider;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.transport.WebServiceMessageSender;

/**
 * Convenient super class for application classes that need Web service access.
 * <p>
 * Requires a {@link WebServiceMessageFactory} or a {@link WebServiceTemplate} instance to
 * be set. It will create its own {@code WebServiceTemplate} if
 * {@code WebServiceMessageFactory} is passed in.
 * <p>
 * In addition to the message factory property, this gateway offers {@link Marshaller} and
 * {@link Unmarshaller} properties. Setting these is required when the
 * {@link WebServiceTemplate#marshalSendAndReceive(Object) marshalling methods} of the
 * template are to be used.
 * <p>
 * Note that when {@link #setWebServiceTemplate(WebServiceTemplate) injecting a
 * WebServiceTemplate directly}, the convenience setters
 * ({@link #setMarshaller(Marshaller)}, {@link #setUnmarshaller(Unmarshaller)},
 * {@link #setMessageSender(WebServiceMessageSender)},
 * {@link #setMessageSenders(WebServiceMessageSender[])}, and
 * {@link #setDefaultUri(String)}) should not be used on this class, but on the template
 * directly.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see #setMessageFactory(WebServiceMessageFactory)
 * @see WebServiceTemplate
 * @see #setMarshaller(Marshaller)
 */
public abstract class WebServiceGatewaySupport implements InitializingBean {

	/** Logger available to subclasses. */
	protected final Log logger = LogFactory.getLog(getClass());

	private WebServiceTemplate webServiceTemplate;

	/**
	 * Creates a new instance of the {@code WebServiceGatewaySupport} class, with a
	 * default {@code WebServiceTemplate}.
	 */
	protected WebServiceGatewaySupport() {
		this.webServiceTemplate = new WebServiceTemplate();
	}

	/**
	 * Creates a new {@code WebServiceGatewaySupport} instance based on the given message
	 * factory.
	 * @param messageFactory the message factory to use
	 */
	protected WebServiceGatewaySupport(WebServiceMessageFactory messageFactory) {
		this.webServiceTemplate = new WebServiceTemplate(messageFactory);
	}

	/** Returns the {@code WebServiceMessageFactory} used by the gateway. */
	public final WebServiceMessageFactory getMessageFactory() {
		return this.webServiceTemplate.getMessageFactory();
	}

	/** Set the {@code WebServiceMessageFactory} to be used by the gateway. */
	public final void setMessageFactory(WebServiceMessageFactory messageFactory) {
		this.webServiceTemplate.setMessageFactory(messageFactory);
	}

	/** Returns the default URI used by the gateway. */
	public final @Nullable String getDefaultUri() {
		return this.webServiceTemplate.getDefaultUri();
	}

	/** Sets the default URI used by the gateway. */
	public final void setDefaultUri(String uri) {
		this.webServiceTemplate.setDefaultUri(uri);
	}

	/** Returns the destination provider used by the gateway. */
	public final @Nullable DestinationProvider getDestinationProvider() {
		return this.webServiceTemplate.getDestinationProvider();
	}

	/** Set the destination provider URI used by the gateway. */
	public final void setDestinationProvider(DestinationProvider destinationProvider) {
		this.webServiceTemplate.setDestinationProvider(destinationProvider);
	}

	/** Sets a single {@code WebServiceMessageSender} to be used by the gateway. */
	public final void setMessageSender(WebServiceMessageSender messageSender) {
		this.webServiceTemplate.setMessageSender(messageSender);
	}

	/** Returns the {@code WebServiceMessageSender}s used by the gateway. */
	public final WebServiceMessageSender[] getMessageSenders() {
		return this.webServiceTemplate.getMessageSenders();
	}

	/** Sets multiple {@code WebServiceMessageSender} to be used by the gateway. */
	public final void setMessageSenders(WebServiceMessageSender[] messageSenders) {
		this.webServiceTemplate.setMessageSenders(messageSenders);
	}

	/** Returns the {@code WebServiceTemplate} for the gateway. */
	public final WebServiceTemplate getWebServiceTemplate() {
		return this.webServiceTemplate;
	}

	/**
	 * Sets the {@code WebServiceTemplate} to be used by the gateway.
	 * <p>
	 * When using this property, the convenience setters
	 * ({@link #setMarshaller(Marshaller)}, {@link #setUnmarshaller(Unmarshaller)},
	 * {@link #setMessageSender(WebServiceMessageSender)},
	 * {@link #setMessageSenders(WebServiceMessageSender[])}, and
	 * {@link #setDefaultUri(String)}) should not be set on this class, but on the
	 * template directly.
	 */
	public final void setWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
		Assert.notNull(webServiceTemplate, "'webServiceTemplate' must not be null");
		this.webServiceTemplate = webServiceTemplate;
	}

	/** Returns the {@code Marshaller} used by the gateway. */
	public final @Nullable Marshaller getMarshaller() {
		return this.webServiceTemplate.getMarshaller();
	}

	/**
	 * Sets the {@code Marshaller} used by the gateway. Setting this property is only
	 * required if the marshalling functionality of {@code WebServiceTemplate} is to be
	 * used.
	 * @see WebServiceTemplate#marshalSendAndReceive
	 */
	public final void setMarshaller(Marshaller marshaller) {
		this.webServiceTemplate.setMarshaller(marshaller);
	}

	/** Returns the {@code Unmarshaller} used by the gateway. */
	public final @Nullable Unmarshaller getUnmarshaller() {
		return this.webServiceTemplate.getUnmarshaller();
	}

	/**
	 * Sets the {@code Unmarshaller} used by the gateway. Setting this property is only
	 * required if the marshalling functionality of {@code WebServiceTemplate} is to be
	 * used.
	 * @see WebServiceTemplate#marshalSendAndReceive
	 */
	public final void setUnmarshaller(Unmarshaller unmarshaller) {
		this.webServiceTemplate.setUnmarshaller(unmarshaller);
	}

	/** Returns the {@code ClientInterceptors} used by the template. */
	public final ClientInterceptor @Nullable [] getInterceptors() {
		return this.webServiceTemplate.getInterceptors();
	}

	/** Sets the {@code ClientInterceptors} used by the gateway. */
	public final void setInterceptors(ClientInterceptor[] interceptors) {
		this.webServiceTemplate.setInterceptors(interceptors);
	}

	@Override
	public final void afterPropertiesSet() throws Exception {
		this.webServiceTemplate.afterPropertiesSet();
		initGateway();
	}

	/**
	 * Subclasses can override this for custom initialization behavior. Gets called after
	 * population of this instance's bean properties.
	 * @throws java.lang.Exception if initialization fails
	 */
	protected void initGateway() throws Exception {
	}

}
