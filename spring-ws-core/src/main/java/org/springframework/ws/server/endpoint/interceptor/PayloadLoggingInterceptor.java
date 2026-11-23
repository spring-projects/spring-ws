/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.interceptor;

import javax.xml.transform.Source;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.server.endpoint.AbstractLoggingInterceptor;

/**
 * Simple {@link org.springframework.ws.server.EndpointInterceptor EndpointInterceptor}
 * that logs the payload of request and response messages.
 * <p>
 * By default, both request and response messages are logged, but this behaviour can be
 * changed using the {@link #setLogRequest(boolean) logRequest} and
 * {@link #setLogResponse(boolean) logResponse} properties.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see #setLogRequest(boolean)
 * @see #setLogResponse(boolean)
 */
public class PayloadLoggingInterceptor extends AbstractLoggingInterceptor {

	@Override
	protected @Nullable Source getSource(WebServiceMessage message) {
		return message.getPayloadSource();
	}

}
