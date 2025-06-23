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

package org.springframework.ws.transport.jms;

import java.nio.charset.StandardCharsets;

import jakarta.annotation.Resource;
import jakarta.jms.BytesMessage;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("jms-receiver-applicationContext.xml")
class WebServiceMessageListenerIntegrationTests {

	private static final String CONTENT = """
			<SOAP-ENV:Envelope xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'>\
			<SOAP-ENV:Body>
			<m:GetLastTradePrice xmlns:m='http://www.springframework.org/spring-ws'>
			<symbol>DIS</symbol>
			</m:GetLastTradePrice>
			</SOAP-ENV:Body></SOAP-ENV:Envelope>""";

	@Autowired
	private JmsTemplate jmsTemplate;

	@Resource
	private Queue responseQueue;

	@Resource
	private Queue requestQueue;

	@Autowired
	private Topic requestTopic;

	@Test
	void testReceiveQueueBytesMessage() {

		final byte[] b = CONTENT.getBytes(StandardCharsets.UTF_8);

		this.jmsTemplate.send(this.requestQueue, session -> {
			BytesMessage request = session.createBytesMessage();
			request.setJMSReplyTo(this.responseQueue);
			request.writeBytes(b);
			return request;
		});

		BytesMessage response = (BytesMessage) this.jmsTemplate.receive(this.responseQueue);

		assertThat(response).isNotNull();
	}

	@Test
	void testReceiveQueueTextMessage() {

		this.jmsTemplate.send(this.requestQueue, session -> {

			TextMessage request = session.createTextMessage(CONTENT);
			request.setJMSReplyTo(this.responseQueue);
			return request;
		});

		TextMessage response = (TextMessage) this.jmsTemplate.receive(this.responseQueue);

		assertThat(response).isNotNull();
	}

	@Test
	void testReceiveTopic() throws Exception {

		final byte[] b = CONTENT.getBytes(StandardCharsets.UTF_8);

		this.jmsTemplate.send(this.requestTopic, session -> {

			BytesMessage request = session.createBytesMessage();
			request.writeBytes(b);
			return request;
		});

		Thread.sleep(100);
	}

}
