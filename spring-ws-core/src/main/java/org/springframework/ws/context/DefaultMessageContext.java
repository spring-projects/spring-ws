/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.context;

import java.io.IOException;
import java.io.InputStream;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

/**
 * Default implementation of {@code MessageContext}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class DefaultMessageContext extends AbstractMessageContext {

	private final WebServiceMessageFactory messageFactory;

	private final WebServiceMessage request;

	private @Nullable WebServiceMessage response;

	/**
	 * Construct a new, empty instance of the {@code DefaultMessageContext} with the given
	 * message factory.
	 */
	public DefaultMessageContext(WebServiceMessageFactory messageFactory) {
		this(messageFactory.createWebServiceMessage(), messageFactory);
	}

	/**
	 * Construct a new instance of the {@code DefaultMessageContext} with the given
	 * request message and message factory.
	 */
	public DefaultMessageContext(WebServiceMessage request, WebServiceMessageFactory messageFactory) {
		Assert.notNull(request, "request must not be null");
		Assert.notNull(messageFactory, "messageFactory must not be null");
		this.request = request;
		this.messageFactory = messageFactory;
	}

	@Override
	public WebServiceMessage getRequest() {
		return this.request;
	}

	@Override
	public boolean hasResponse() {
		return this.response != null;
	}

	@Override
	public WebServiceMessage getResponse() {
		if (this.response == null) {
			this.response = this.messageFactory.createWebServiceMessage();
		}
		return this.response;
	}

	@Override
	public void setResponse(@Nullable WebServiceMessage response) {
		checkForResponse();
		this.response = response;
	}

	@Override
	public void clearResponse() {
		this.response = null;
	}

	@Override
	public void readResponse(InputStream inputStream) throws IOException {
		checkForResponse();
		this.response = this.messageFactory.createWebServiceMessage(inputStream);
	}

	public WebServiceMessageFactory getMessageFactory() {
		return this.messageFactory;
	}

	private void checkForResponse() throws IllegalStateException {
		if (this.response != null) {
			throw new IllegalStateException("Response message already created");
		}
	}

}
