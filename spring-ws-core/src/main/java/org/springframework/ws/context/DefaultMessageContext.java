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

package org.springframework.ws.context;

import java.io.IOException;
import java.io.InputStream;

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

	private WebServiceMessage response;

	/** Construct a new, empty instance of the {@code DefaultMessageContext} with the given message factory. */
	public DefaultMessageContext(WebServiceMessageFactory messageFactory) {
		this(messageFactory.createWebServiceMessage(), messageFactory);
	}

	/**
	 * Construct a new instance of the {@code DefaultMessageContext} with the given request message and message
	 * factory.
	 */
	public DefaultMessageContext(WebServiceMessage request, WebServiceMessageFactory messageFactory) {
		Assert.notNull(request, "request must not be null");
		Assert.notNull(messageFactory, "messageFactory must not be null");
		this.request = request;
		this.messageFactory = messageFactory;
	}

	@Override
	public WebServiceMessage getRequest() {
		return request;
	}

	@Override
	public boolean hasResponse() {
		return response != null;
	}

	@Override
	public WebServiceMessage getResponse() {
		if (response == null) {
			response = messageFactory.createWebServiceMessage();
		}
		return response;
	}

	@Override
	public void setResponse(WebServiceMessage response) {
		checkForResponse();
		this.response = response;
	}

	@Override
	public void clearResponse() {
		response = null;
	}

	@Override
	public void readResponse(InputStream inputStream) throws IOException {
		checkForResponse();
		response = messageFactory.createWebServiceMessage(inputStream);
	}

	public WebServiceMessageFactory getMessageFactory() {
		return messageFactory;
	}

	private void checkForResponse() throws IllegalStateException {
		if (response != null) {
			throw new IllegalStateException("Response message already created");
		}
	}
}
