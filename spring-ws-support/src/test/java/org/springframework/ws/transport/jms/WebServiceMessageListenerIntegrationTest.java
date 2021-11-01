/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.transport.jms;

import static org.assertj.core.api.Assertions.*;

import jakarta.annotation.Resource;
import jakarta.jms.BytesMessage;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;

import java.nio.charset.StandardCharsets;

import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("jms-receiver-applicationContext.xml")
public class WebServiceMessageListenerIntegrationTest {

	private static final String CONTENT = "<SOAP-ENV:Envelope xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'>"
			+ "<SOAP-ENV:Body>\n" + "<m:GetLastTradePrice xmlns:m='http://www.springframework.org/spring-ws'>\n"
			+ "<symbol>DIS</symbol>\n" + "</m:GetLastTradePrice>\n" + "</SOAP-ENV:Body></SOAP-ENV:Envelope>";

	@Autowired private JmsTemplate jmsTemplate;

	@Resource private Queue responseQueue;

	@Resource private Queue requestQueue;

	@Autowired private Topic requestTopic;

	private EmbeddedActiveMQ server;

	@BeforeEach
	void setUp() throws Exception {

		Configuration config = new ConfigurationImpl();
		config.addAcceptorConfiguration("vm", "vm://0");
		config.addAcceptorConfiguration("tcp", "tcp://127.0.0.1:61616");
		config.setSecurityEnabled(false);
		server = new EmbeddedActiveMQ();
		server.setConfiguration(config);
		server.start();
	}

	@AfterEach
	void tearDown() throws Exception {
		server.stop();
	}

	@Test
	public void testReceiveQueueBytesMessage() throws Exception {

		final byte[] b = CONTENT.getBytes(StandardCharsets.UTF_8);

		jmsTemplate.send(requestQueue, session -> {
			BytesMessage request = session.createBytesMessage();
			request.setJMSReplyTo(responseQueue);
			request.writeBytes(b);
			return request;
		});

		BytesMessage response = (BytesMessage) jmsTemplate.receive(responseQueue);

		assertThat(response).isNotNull();
	}

	@Test
	public void testReceiveQueueTextMessage() {

		jmsTemplate.send(requestQueue, session -> {

			TextMessage request = session.createTextMessage(CONTENT);
			request.setJMSReplyTo(responseQueue);
			return request;
		});

		TextMessage response = (TextMessage) jmsTemplate.receive(responseQueue);

		assertThat(response).isNotNull();
	}

	@Test
	public void testReceiveTopic() throws Exception {

		final byte[] b = CONTENT.getBytes(StandardCharsets.UTF_8);

		jmsTemplate.send(requestTopic, session -> {

			BytesMessage request = session.createBytesMessage();
			request.writeBytes(b);
			return request;
		});

		Thread.sleep(100);
	}
}
