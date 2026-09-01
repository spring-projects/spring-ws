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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.tck.TestObservationRegistry;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Message;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConstants;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.server.endpoint.mapping.PayloadRootQNameEndpointMapping;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.SoapMessageDispatcher;
import org.springframework.ws.transport.test.EchoPayloadEndpoint;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Tests for JMS transport with observations.
 *
 * @author Stephane Nicoll
 */
@SpringJUnitConfig
class JmsObservationIntegrationTests {

	private static final String SERVER_REQUEST_QUEUE_NAME = "RequestQueue";

	private static final String CLIENT_REQUEST_QUEUE_NAME = "ClientRequestQueue";

	private static final String CLIENT_RESPONSE_QUEUE_NAME = "ClientResponseQueue";

	private static final String PAYLOAD = """
			<m:GetLastTradePrice xmlns:m='http://www.springframework.org/spring-ws'>
			    <symbol>DIS</symbol>
			</m:GetLastTradePrice>""";

	private static final String ENVELOPE = """
			<SOAP-ENV:Envelope xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'>
			    <SOAP-ENV:Body>
			%s
			    </SOAP-ENV:Body>
			</SOAP-ENV:Envelope>""".formatted(PAYLOAD);

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private WebServiceTemplate webServiceTemplate;

	@Autowired
	private TestObservationRegistry observationRegistry;

	@Test
	void receiveMessageCreatesServerObservation() {
		this.jmsTemplate.sendAndReceive(SERVER_REQUEST_QUEUE_NAME, session -> session.createTextMessage(ENVELOPE));
		await().untilAsserted(
				() -> assertThat(this.observationRegistry).hasObservationWithNameEqualTo("soap.server.requests")
					.that()
					.hasBeenStopped()
					.doesNotHaveError()
					.hasContextualNameEqualTo("soap GetLastTradePrice")
					.hasLowCardinalityKeyValue("exception", "none")
					.hasLowCardinalityKeyValue("fault.code", "none")
					.hasLowCardinalityKeyValue("namespace", "http://www.springframework.org/spring-ws")
					.hasLowCardinalityKeyValue("operation.name", "GetLastTradePrice")
					.hasLowCardinalityKeyValue("outcome", "SUCCESS")
					.hasLowCardinalityKeyValue("protocol", "jms")
					.hasHighCardinalityKeyValue("fault.reason", "none")
					.hasHighCardinalityKeyValue("uri", "jms:RequestQueue"));
	}

	@Test
	void sendMessageCreatesClientObservation() {
		this.webServiceTemplate.sendSourceAndReceiveToResult("jms:" + CLIENT_REQUEST_QUEUE_NAME + "?replyToName="
				+ CLIENT_RESPONSE_QUEUE_NAME + "&deliveryMode=NON_PERSISTENT", new StringSource(PAYLOAD),
				new StringResult());

		assertThat(this.observationRegistry).hasObservationWithNameEqualTo("soap.client.requests")
			.that()
			.hasBeenStopped()
			.doesNotHaveError()
			.hasContextualNameEqualTo("soap GetLastTradePrice")
			.hasLowCardinalityKeyValue("exception", "none")
			.hasLowCardinalityKeyValue("fault.code", "none")
			.hasLowCardinalityKeyValue("namespace", "http://www.springframework.org/spring-ws")
			.hasLowCardinalityKeyValue("operation.name", "GetLastTradePrice")
			.hasLowCardinalityKeyValue("outcome", "SUCCESS")
			.hasLowCardinalityKeyValue("protocol", "jms")
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
		DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
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
				ObservationRegistry observationRegistry) {
			WebServiceTemplate template = new WebServiceTemplate(messageFactory);
			template.setMessageSender(messageSender);
			template.setObservationRegistry(observationRegistry);
			return template;
		}

		@Bean
		PayloadRootQNameEndpointMapping endpointMapping() {
			PayloadRootQNameEndpointMapping endpointMapping = new PayloadRootQNameEndpointMapping();
			endpointMapping.setEndpointMap(
					Map.of("{http://www.springframework.org/spring-ws}GetLastTradePrice", new EchoPayloadEndpoint()));
			return endpointMapping;
		}

		@Bean
		SoapMessageDispatcher messageDispatcher(EndpointMapping endpointMapping) {
			SoapMessageDispatcher messageDispatcher = new SoapMessageDispatcher();
			messageDispatcher.setEndpointMappings(List.of(endpointMapping));
			return messageDispatcher;
		}

		@Bean
		WebServiceMessageListener webServiceMessageListener(WebServiceMessageFactory messageFactory,
				SoapMessageDispatcher messageDispatcher, ObservationRegistry observationRegistry) {
			WebServiceMessageListener webServiceMessageListener = new WebServiceMessageListener();
			webServiceMessageListener.setMessageReceiver(messageDispatcher);
			webServiceMessageListener.setMessageFactory(messageFactory);
			webServiceMessageListener.setObservationRegistry(observationRegistry);
			return webServiceMessageListener;
		}

		@Bean
		DefaultMessageListenerContainer requestQueueMessageListenerContainer(ConnectionFactory connectionFactory,
				WebServiceMessageListener messageListener) {
			DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
			container.setConnectionFactory(connectionFactory);
			container.setDestinationName(SERVER_REQUEST_QUEUE_NAME);
			container.setMessageListener(messageListener);
			return container;
		}

		@Bean
		JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
			return new JmsTemplate(connectionFactory);
		}

	}

	/**
	 * Replies to the client requests with an empty SOAP envelope, so that the client
	 * observation can be completed without standing up a full server.
	 */
	static class TestJmsListener {

		@JmsListener(destination = CLIENT_REQUEST_QUEUE_NAME)
		@SendTo(CLIENT_RESPONSE_QUEUE_NAME)
		org.springframework.messaging.Message<String> handleRequest(Message request) throws Exception {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL).createMessage().writeTo(out);
			return MessageBuilder.withPayload(out.toString(StandardCharsets.UTF_8))
				.setHeader(JmsTransportConstants.PROPERTY_CONTENT_TYPE, SoapVersion.SOAP_11.getContentType())
				.build();
		}

	}

}
