/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.transport.support;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageReceiver;

/**
 * Base class for server-side transport objects which have a predefined {@link WebServiceMessageReceiver}.
 *
 * @author Arjen Poutsma
 * @see #handleConnection(WebServiceConnection)
 * @since 1.5.0
 */
public abstract class SimpleWebServiceMessageReceiverObjectSupport extends WebServiceMessageReceiverObjectSupport
		implements InitializingBean {

	private WebServiceMessageReceiver messageReceiver;

	/**
	 * Returns the {@code WebServiceMessageReceiver} used by this listener.
	 */
	public WebServiceMessageReceiver getMessageReceiver() {
		return messageReceiver;
	}

	/**
	 * Sets the {@code WebServiceMessageReceiver} used by this listener.
	 */
	public void setMessageReceiver(WebServiceMessageReceiver messageReceiver) {
		this.messageReceiver = messageReceiver;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		Assert.notNull(getMessageReceiver(), "messageReceiver must not be null");
	}

	protected final void handleConnection(WebServiceConnection connection) throws Exception {
		handleConnection(connection, getMessageReceiver());
	}

}
