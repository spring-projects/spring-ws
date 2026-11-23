/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint;

import javax.xml.transform.Source;

import org.jspecify.annotations.Nullable;

/**
 * Defines the basic contract for Web Services interested in just the message payload.
 * <p>
 * The main entrypoint is {@link #invoke(Source)}, which gets invoked with the contents of
 * the requesting message.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface PayloadEndpoint {

	/**
	 * Invokes the endpoint with the given request payload, and possibly returns a
	 * response.
	 * @param request the payload of the request message, may be {@code null}
	 * @return the payload of the response message, may be {@code null} to indicate no
	 * response
	 * @throws Exception if an exception occurs
	 */
	@Nullable Source invoke(@Nullable Source request) throws Exception;

}
