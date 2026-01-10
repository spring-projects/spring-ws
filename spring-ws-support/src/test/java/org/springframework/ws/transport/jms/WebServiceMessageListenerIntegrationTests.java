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
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.destination.BeanFactoryDestinationResolver;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.SimpleTestingMessageReceiver;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
class WebServiceMessageListenerIntegrationTests {

	private static final String REQUEST_QUEUE_NAME = "RequestQueue";

	private static final String REQUEST_TOPIC_NAME = "RequestTopic";

	private static final String RESPONSE_QUEUE_NAME = "ResponseQueue";

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
	private Queue requestQueue;

	@Autowired
	private Topic requestTopic;

	@Resource
	private Queue responseQueue;

	@Test
	void testReceiveQueueBytesMessage() {
		final byte[] b = CONTENT.getBytes(StandardCharsets.UTF_8);
		this.jmsTemplate.send(this.requestQueue, session -> {
			BytesMessage request = session.createBytesMessage();
			request.setJMSReplyTo(this.responseQueue);
			request.writeBytes(b);
			return request;
		});
		assertThat(this.jmsTemplate.receive(this.responseQueue)).isInstanceOf(BytesMessage.class);
	}

	@Test
	void testReceiveQueueTextMessage() {
		this.jmsTemplate.send(this.requestQueue, session -> {
			TextMessage request = session.createTextMessage(CONTENT);
			request.setJMSReplyTo(this.responseQueue);
			return request;
		});
		assertThat(this.jmsTemplate.receive(this.responseQueue)).isInstanceOf(TextMessage.class);
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

	@Configuration(proxyBeanMethods = false)
	static class Config {

		@Bean
		ActiveMQConnectionFactory connectionFactory() {
			return new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
		}

		@Bean
		Queue requestQueue() {
			return new ActiveMQQueue(REQUEST_QUEUE_NAME);
		}

		@Bean
		Topic requestTopic() {
			return new ActiveMQTopic(REQUEST_TOPIC_NAME);
		}

		@Bean
		Queue responseQueue() {
			return new ActiveMQQueue(RESPONSE_QUEUE_NAME);
		}

		@Bean
		JmsTemplate jmsTemplate(ConnectionFactory connectionFactory, BeanFactory beanFactory) {
			JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
			jmsTemplate.setDestinationResolver(new BeanFactoryDestinationResolver(beanFactory));
			return jmsTemplate;
		}

		@Bean
		SaajSoapMessageFactory messageFactory() {
			return new SaajSoapMessageFactory();
		}

		@Bean
		WebServiceMessageListener webServiceMessageListener(WebServiceMessageFactory messageFactory) {
			WebServiceMessageListener webServiceMessageListener = new WebServiceMessageListener();
			webServiceMessageListener.setMessageFactory(messageFactory);
			webServiceMessageListener.setMessageReceiver(new SimpleTestingMessageReceiver());
			return webServiceMessageListener;
		}

		@Bean
		DefaultMessageListenerContainer requestQueueMessageListenerContainer(ConnectionFactory connectionFactory,
				WebServiceMessageListener messageListener) {
			DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
			container.setConnectionFactory(connectionFactory);
			container.setDestinationName(REQUEST_QUEUE_NAME);
			container.setMessageListener(messageListener);
			return container;
		}

		@Bean
		DefaultMessageListenerContainer requestTopicMessageListenerContainer(ConnectionFactory connectionFactory,
				WebServiceMessageListener messageListener) {
			DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
			container.setConnectionFactory(connectionFactory);
			container.setDestinationName(REQUEST_TOPIC_NAME);
			container.setMessageListener(messageListener);
			return container;
		}

	}

}
