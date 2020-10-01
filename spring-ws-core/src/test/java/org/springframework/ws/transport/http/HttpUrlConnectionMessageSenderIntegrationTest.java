/*
 * Copyright 2005-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.http;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.Test;

public class HttpUrlConnectionMessageSenderIntegrationTest
		extends AbstractHttpWebServiceMessageSenderIntegrationTestCase<HttpUrlConnectionMessageSender> {

	@Override
	protected HttpUrlConnectionMessageSender createMessageSender() {
		return new HttpUrlConnectionMessageSender();
	}

	@Test
	public void testSetTimeout() throws Exception {

		this.messageSender.setConnectionTimeout(Duration.ofSeconds(3));
		this.messageSender.setReadTimeout(Duration.ofSeconds(5));

		try (HttpUrlConnection connection = (HttpUrlConnection) this.messageSender.createConnection(this.connectionUri)) {

			assertThat(connection.getConnection().getConnectTimeout()).isEqualTo(3000);
			assertThat(connection.getConnection().getReadTimeout()).isEqualTo(5000);
		}
	}
}
