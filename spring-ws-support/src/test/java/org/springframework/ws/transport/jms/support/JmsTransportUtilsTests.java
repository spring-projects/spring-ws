/*
 * Copyright 2005-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.jms.support;

import java.net.URI;

import jakarta.jms.DeliveryMode;
import jakarta.jms.Message;
import org.junit.jupiter.api.Test;

import org.springframework.ws.transport.jms.JmsTransportConstants;

import static org.assertj.core.api.Assertions.assertThat;

class JmsTransportUtilsTests {

	@Test
	void getDestinationName() throws Exception {

		URI uri = new URI("jms:RequestQueue?replyToName=RESP_QUEUE");
		String destinationName = JmsTransportUtils.getDestinationName(uri);

		assertThat(destinationName).isEqualTo("RequestQueue");

		uri = new URI("jms:RequestQueue");
		destinationName = JmsTransportUtils.getDestinationName(uri);

		assertThat(destinationName).isEqualTo("RequestQueue");
	}

	@Test
	void getDeliveryMode() throws Exception {

		URI uri = new URI("jms:RequestQueue?deliveryMode=NON_PERSISTENT");
		int deliveryMode = JmsTransportUtils.getDeliveryMode(uri);

		assertThat(deliveryMode).isEqualTo(DeliveryMode.NON_PERSISTENT);

		uri = new URI("jms:RequestQueue?deliveryMode=PERSISTENT");
		deliveryMode = JmsTransportUtils.getDeliveryMode(uri);

		assertThat(deliveryMode).isEqualTo(DeliveryMode.PERSISTENT);

		uri = new URI("jms:RequestQueue?replyToName=RESP_QUEUE");
		deliveryMode = JmsTransportUtils.getDeliveryMode(uri);

		assertThat(deliveryMode).isEqualTo(Message.DEFAULT_DELIVERY_MODE);
	}

	@Test
	void getMessageType() throws Exception {

		URI uri = new URI("jms:RequestQueue?messageType=BYTESMESSAGE");
		int messageType = JmsTransportUtils.getMessageType(uri);

		assertThat(messageType).isEqualTo(JmsTransportConstants.BYTES_MESSAGE_TYPE);

		uri = new URI("jms:RequestQueue?messageType=TEXT_MESSAGE");
		messageType = JmsTransportUtils.getMessageType(uri);

		assertThat(messageType).isEqualTo(JmsTransportConstants.TEXT_MESSAGE_TYPE);

		uri = new URI("jms:RequestQueue?replyToName=RESP_QUEUE");
		messageType = JmsTransportUtils.getMessageType(uri);

		assertThat(messageType).isEqualTo(JmsTransportConstants.BYTES_MESSAGE_TYPE);
	}

	@Test
	void getTimeToLive() throws Exception {

		URI uri = new URI("jms:RequestQueue?timeToLive=100");
		long timeToLive = JmsTransportUtils.getTimeToLive(uri);

		assertThat(timeToLive).isEqualTo(100);

		uri = new URI("jms:RequestQueue?replyToName=RESP_QUEUE");
		timeToLive = JmsTransportUtils.getTimeToLive(uri);

		assertThat(timeToLive).isEqualTo(Message.DEFAULT_TIME_TO_LIVE);
	}

	@Test
	void getPriority() throws Exception {

		URI uri = new URI("jms:RequestQueue?priority=5");
		int priority = JmsTransportUtils.getPriority(uri);

		assertThat(priority).isEqualTo(5);

		uri = new URI("jms:RequestQueue?replyToName=RESP_QUEUE");
		priority = JmsTransportUtils.getPriority(uri);

		assertThat(priority).isEqualTo(Message.DEFAULT_PRIORITY);
	}

	@Test
	void getReplyToName() throws Exception {

		URI uri = new URI("jms:RequestQueue?replyToName=RESP_QUEUE");
		String replyToName = JmsTransportUtils.getReplyToName(uri);

		assertThat(replyToName).isEqualTo("RESP_QUEUE");

		uri = new URI("jms:RequestQueue?priority=5");
		replyToName = JmsTransportUtils.getReplyToName(uri);

		assertThat(replyToName).isNull();
	}

	@Test
	void jndi() throws Exception {

		URI uri = new URI("jms:jms/REQUEST_QUEUE?replyToName=jms/REPLY_QUEUE");
		String destination = JmsTransportUtils.getDestinationName(uri);

		assertThat(destination).isEqualTo("jms/REQUEST_QUEUE");

		String replyTo = JmsTransportUtils.getReplyToName(uri);

		assertThat(replyTo).isEqualTo("jms/REPLY_QUEUE");
	}

}
