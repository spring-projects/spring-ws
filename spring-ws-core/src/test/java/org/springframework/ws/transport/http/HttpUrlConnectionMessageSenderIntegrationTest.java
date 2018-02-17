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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.IOException;

public class HttpUrlConnectionMessageSenderIntegrationTest
		extends AbstractHttpWebServiceMessageSenderIntegrationTestCase<HttpUrlConnectionMessageSender> {

	@Override
	protected HttpUrlConnectionMessageSender createMessageSender() {
		return new HttpUrlConnectionMessageSender();
	}

	@Test
	public void testSetTimeout() throws Exception {

		messageSender.setConnectionTimeout(3000);
		messageSender.setReadTimeout(5000);

		HttpUrlConnection connection = null;
		try {
			connection = HttpUrlConnection.class.cast(messageSender.createConnection(connectionUri));
			assertEquals(3000, connection.getConnection().getConnectTimeout());
			assertEquals(5000, connection.getConnection().getReadTimeout());
		}
		finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (IOException ex) {
					// ignore
				}
			}
		}
	}

}