/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.addressing.messageid;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UuidMessageIdStrategyTests {

	private MessageIdStrategy strategy;

	@BeforeEach
	final void setUp() {
		this.strategy = new UuidMessageIdStrategy();
	}

	@Test
	void testStrategy() {

		URI messageId1 = this.strategy.newMessageId(null);

		assertThat(messageId1).isNotNull();

		URI messageId2 = this.strategy.newMessageId(null);

		assertThat(messageId2).isNotNull();
		assertThat(messageId2).isNotEqualTo(messageId1);
	}

}
