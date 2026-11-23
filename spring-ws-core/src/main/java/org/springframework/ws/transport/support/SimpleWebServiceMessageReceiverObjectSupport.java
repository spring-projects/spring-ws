/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.support;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageReceiver;

/**
 * Base class for server-side transport objects which have a predefined
 * {@link WebServiceMessageReceiver}.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 * @see #handleConnection(WebServiceConnection)
 */
public abstract class SimpleWebServiceMessageReceiverObjectSupport extends WebServiceMessageReceiverObjectSupport
		implements InitializingBean {

	@SuppressWarnings("NullAway.Init")
	private WebServiceMessageReceiver messageReceiver;

	/**
	 * Returns the {@code WebServiceMessageReceiver} used by this listener.
	 */
	public WebServiceMessageReceiver getMessageReceiver() {
		return this.messageReceiver;
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
