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

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.tck.TestObservationRegistry;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.SimpleTestingMessageReceiver;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link WebServiceMessageListener} with observations.
 *
 * @author Stephane Nicoll
 */
@SpringJUnitConfig
class WebServiceMessageListenerObservationIntegrationTests {

	private static final String REQUEST_QUEUE_NAME = "RequestQueue";

	private static final String CONTENT = """
			<SOAP-ENV:Envelope xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'>
				<SOAP-ENV:Body>
					<m:GetLastTradePrice xmlns:m='http://www.springframework.org/spring-ws'>
						<symbol>DIS</symbol>
					</m:GetLastTradePrice>
				</SOAP-ENV:Body>
			</SOAP-ENV:Envelope>""";

	private final JmsTemplate jmsTemplate;

	private final TestObservationRegistry observationRegistry;

	WebServiceMessageListenerObservationIntegrationTests(@Autowired ConnectionFactory connectionFactory,
			@Autowired TestObservationRegistry observationRegistry) {
		this.jmsTemplate = new JmsTemplate(connectionFactory);
		this.observationRegistry = observationRegistry;
	}

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

	@Configuration(proxyBeanMethods = false)
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

	}

}
