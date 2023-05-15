package org.springframework.ws.transport.http;

import java.time.Duration;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class HttpComponents5MessageSenderTest {

	@Test
	void afterPropertiesSet_createHttpClient() throws Exception {
		HttpComponents5MessageSender messageSender = new HttpComponents5MessageSender();
		assertThat(messageSender.getHttpClient()).isNull();
		Duration timeout = Duration.ofSeconds(1);
		assertThatCode(() -> messageSender.setConnectionTimeout(timeout)).doesNotThrowAnyException();
		messageSender.afterPropertiesSet();
		assertThat(messageSender.getHttpClient()).isNotNull();
	}

	@Test
	void afterPropertiesSet_httpClientAlreadySet() throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpComponents5MessageSender messageSender = new HttpComponents5MessageSender(httpClient);
		Duration timeout = Duration.ofSeconds(1);
		assertThatCode(() -> messageSender.setConnectionTimeout(timeout)).isInstanceOf(IllegalStateException.class);
		messageSender.afterPropertiesSet();
		assertThat(messageSender.getHttpClient()).isSameAs(httpClient);
	}

}