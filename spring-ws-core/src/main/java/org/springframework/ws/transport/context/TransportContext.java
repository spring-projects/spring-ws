/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.context;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.transport.WebServiceConnection;

/**
 * Strategy interface for determining the current
 * {@link org.springframework.ws.transport.WebServiceConnection}.
 * <p>
 * An instance of this class can be associated with a thread via the
 * {@link TransportContextHolder} class.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface TransportContext {

	/** Returns the current {@code WebServiceConnection}. */
	@Nullable WebServiceConnection getConnection();

}
