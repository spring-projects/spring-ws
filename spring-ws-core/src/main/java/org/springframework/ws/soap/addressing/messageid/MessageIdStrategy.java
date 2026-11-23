/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.addressing.messageid;

import java.net.URI;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.soap.SoapMessage;

/**
 * Strategy interface that encapsulates the creation and validation of WS-Addressing
 * {@code MessageID}s.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public interface MessageIdStrategy {

	/**
	 * Indicates whether the given {@code MessageID} value is a duplicate or not.
	 * @param messageId the message id
	 * @return {@code true} if a duplicate; {@code false} otherwise
	 */
	boolean isDuplicate(@Nullable URI messageId);

	/**
	 * Returns a new WS-Addressing {@code MessageID} for the given {@link SoapMessage}.
	 * @param message the message to create an id for
	 * @return the new message id
	 */
	URI newMessageId(SoapMessage message);

}
