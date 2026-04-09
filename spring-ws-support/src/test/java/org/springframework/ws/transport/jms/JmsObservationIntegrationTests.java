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

import java.time.Duration;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.tck.TestObservationRegistry;
import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.SimpleTestingMessageReceiver;
import org.springframework.context.annotation.Import;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.messaging.handler.annotation.SendTo;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for JMS transport with observations.
 *
 * @author Stephane Nicoll
 */
@SpringJUnitConfig
class JmsObservationIntegrationTests {

	private static final String REQUEST_QUEUE_NAME = "RequestQueue";

	private static final String CLIENT_REQUEST_QUEUE_NAME = "ClientRequestQueue";

	private static final String CLIENT_RESPONSE_QUEUE_NAME = "ClientResponseQueue";

	private static final String CONTENT = """
			<SOAP-ENV:Envelope xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'>
				<SOAP-ENV:Body>
					<m:GetLastTradePrice xmlns:m='http://www.springframework.org/spring-ws'>
						<symbol>DIS</symbol>
					</m:GetLastTradePrice>
				</SOAP-ENV:Body>
			</SOAP-ENV:Envelope>""";

	private static final String CLIENT_CONTENT = """
			<m:GetLastTradePrice xmlns:m='http://www.springframework.org/spring-ws'>
				<symbol>DIS</symbol>
			</m:GetLastTradePrice>""";

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private WebServiceTemplate webServiceTemplate;

	@Autowired
	private TestObservationRegistry observationRegistry;

	@Test
	void sendMessageCreateServerObservation() {
		this.jmsTemplate.sendAndReceive(REQUEST_QUEUE_NAME, session -> session.createTextMessage(CONTENT));
		assertThat(this.observationRegistry).hasObservationWithNameEqualTo("soap.server.requests")
			.that()
			.hasBeenStopped()
			.doesNotHaveError()
			.hasLowCardinalityKeyValue("fault.code", "none")
			.hasLowCardinalityKeyValue("protocol", "jms")
			.hasLowCardinalityKeyValue("namespace", "http://www.springframework.org/spring-ws")
			.hasLowCardinalityKeyValue("operation.name", "GetLastTradePrice")
			.hasHighCardinalityKeyValue("fault.reason", "none")
			.hasHighCardinalityKeyValue("uri", "jms:RequestQueue");
	}

	@Test
	void sendMessageCreateClientObservation() {
		this.webServiceTemplate.sendSourceAndReceiveToResult(
				"jms:" + CLIENT_REQUEST_QUEUE_NAME + "?replyToName=" + CLIENT_RESPONSE_QUEUE_NAME
						+ "&deliveryMode=NON_PERSISTENT",
				new org.springframework.xml.transform.StringSource(CLIENT_CONTENT),
				new org.springframework.xml.transform.StringResult());

		assertThat(this.observationRegistry).hasObservationWithNameEqualTo("soap.client.requests")
			.that()
			.hasBeenStopped()
			.doesNotHaveError()
			.hasLowCardinalityKeyValue("fault.code", "none")
			.hasLowCardinalityKeyValue("protocol", "jms")
			.hasLowCardinalityKeyValue("namespace", "http://www.springframework.org/spring-ws")
			.hasLowCardinalityKeyValue("operation.name", "GetLastTradePrice")
			.hasHighCardinalityKeyValue("fault.reason", "none")
			.hasHighCardinalityKeyValue("uri", "jms:ClientRequestQueue");
	}

	@Configuration(proxyBeanMethods = false)
	@EnableJms
	@Import(TestJmsListener.class)
	static class Config {

		@Bean
		ActiveMQConnectionFactory connectionFactory() {
			return new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
		}

		@Bean
		SaajSoapMessageFactory messageFactory() {
			return new SaajSoapMessageFactory();
		}

		@Bean
		TestObservationRegistry observationRegistry() {
			return TestObservationRegistry.create();
		}

		@Bean
		public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
			DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
			factory.setConnectionFactory(connectionFactory);
			return factory;
		}

		@Bean
		JmsMessageSender messageSender(ConnectionFactory connectionFactory) {
			JmsMessageSender messageSender = new JmsMessageSender(connectionFactory);
			messageSender.setReceiveTimeout(Duration.ofSeconds(10).toMillis());
			return messageSender;
		}

		@Bean
		WebServiceTemplate webServiceTemplate(SaajSoapMessageFactory messageFactory, JmsMessageSender messageSender,
				TestObservationRegistry observationRegistry) {
			WebServiceTemplate template = new WebServiceTemplate(messageFactory);
			template.setMessageSender(messageSender);
			template.setObservationRegistry(observationRegistry);
			return template;
		}

		@Bean
		WebServiceMessageListener webServiceMessageListener(WebServiceMessageFactory messageFactory,
				ObservationRegistry observationRegistry) {
			WebServiceMessageListener webServiceMessageListener = new WebServiceMessageListener();
			webServiceMessageListener.setMessageReceiver(new SimpleTestingMessageReceiver());
			webServiceMessageListener.setMessageFactory(messageFactory);
			webServiceMessageListener.setObservationRegistry(observationRegistry);
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
		JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
			return new JmsTemplate(connectionFactory);
		}

	}

	static class TestJmsListener {

		@JmsListener(destination = CLIENT_REQUEST_QUEUE_NAME)
		@SendTo(CLIENT_RESPONSE_QUEUE_NAME)
		public Object handleRequest(jakarta.jms.Message request) throws Exception {
			java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
			jakarta.xml.soap.MessageFactory.newInstance(jakarta.xml.soap.SOAPConstants.SOAP_1_1_PROTOCOL)
				.createMessage()
				.writeTo(bos);
			String text = bos.toString(java.nio.charset.StandardCharsets.UTF_8);
			return org.springframework.messaging.support.MessageBuilder.withPayload(text)
				.setHeader(JmsTransportConstants.PROPERTY_CONTENT_TYPE,
						org.springframework.ws.soap.SoapVersion.SOAP_11.getContentType())
				.build();
		}

	}

}
