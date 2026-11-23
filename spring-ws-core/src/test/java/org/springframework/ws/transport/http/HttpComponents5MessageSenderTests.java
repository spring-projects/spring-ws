/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.http;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link HttpComponents5MessageSender}.
 *
 * @author Lars Uffmann
 * @author Stephane Nicoll
 */
class HttpComponents5MessageSenderTests {

	@Test
	void afterPropertiesSetShouldProperlyInitializeHttpClient() throws Exception {
		HttpComponents5MessageSender messageSender = new HttpComponents5MessageSender();
		assertThat(messageSender).hasFieldOrPropertyWithValue("httpClient", null);
		messageSender.setConnectionTimeout(Duration.ofSeconds(1));

		messageSender.afterPropertiesSet();
		assertThat(messageSender.getHttpClient()).isNotNull();
	}

}
