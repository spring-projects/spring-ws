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

class HttpUrlConnectionMessageSenderIntegrationTests
		extends AbstractHttpWebServiceMessageSenderIntegrationTests<HttpUrlConnectionMessageSender> {

	@Override
	protected HttpUrlConnectionMessageSender createMessageSender() {
		return new HttpUrlConnectionMessageSender();
	}

	@Test
	void testSetTimeout() throws Exception {

		this.messageSender.setConnectionTimeout(Duration.ofSeconds(3));
		this.messageSender.setReadTimeout(Duration.ofSeconds(5));

		try (HttpUrlConnection connection = (HttpUrlConnection) this.messageSender
			.createConnection(this.connectionUri)) {

			assertThat(connection.getConnection().getConnectTimeout()).isEqualTo(3000);
			assertThat(connection.getConnection().getReadTimeout()).isEqualTo(5000);
		}
	}

}
