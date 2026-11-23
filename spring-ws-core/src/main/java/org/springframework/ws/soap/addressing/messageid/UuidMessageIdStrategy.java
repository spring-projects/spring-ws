/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.addressing.messageid;

import java.net.URI;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.soap.SoapMessage;

/**
 * Implementation of the {@link MessageIdStrategy} interface that uses a {@link UUID} to
 * generate a Message Id. The UUID is prefixed by {@value PREFIX}.
 * <p>
 * Note that the {@link UUID} class is only available on Java 5 and above.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class UuidMessageIdStrategy implements MessageIdStrategy {

	/**
	 * Prefix of the generated UUID.
	 */
	public static final String PREFIX = "urn:uuid:";

	/** Returns {@code false}. */
	@Override
	public boolean isDuplicate(@Nullable URI messageId) {
		return false;
	}

	@Override
	public URI newMessageId(SoapMessage message) {
		return URI.create(PREFIX + UUID.randomUUID());
	}

}
