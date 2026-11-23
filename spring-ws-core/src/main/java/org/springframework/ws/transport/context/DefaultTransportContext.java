/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.context;

import org.springframework.util.Assert;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Default implementation of the {@code TransportContext} interface.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class DefaultTransportContext implements TransportContext {

	private final WebServiceConnection connection;

	/**
	 * Creates a new {@code DefaultTransportContext} that exposes the given connection.
	 */
	public DefaultTransportContext(WebServiceConnection connection) {
		Assert.notNull(connection, "'connection' must not be null");
		this.connection = connection;
	}

	@Override
	public WebServiceConnection getConnection() {
		return this.connection;
	}

	public String toString() {
		return this.connection.toString();
	}

}
