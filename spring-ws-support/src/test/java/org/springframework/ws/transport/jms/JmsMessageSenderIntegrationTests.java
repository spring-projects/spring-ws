/*
 * Copyright 2005-2025 the original author or authors.
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
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import jakarta.jms.BytesMessage;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.QueueBrowser;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPException;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.function.ThrowingFunction;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.WebServiceConnection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
class JmsMessageSenderIntegrationTests {

	private static final String SOAP_ACTION = "\"http://springframework.org/DoIt\"";

	private static final MessageFactory messageFactory = createMessageFactory();

	@Autowired
	private ConnectionFactory connectionFactory;

	@Autowired
	private JmsMessageSender messageSender;

	@Autowired
	private TestJmsListener testJmsListener;

	@Test
	void testSendAndReceiveQueueBytesMessageTemporaryQueue() throws Exception {
		URI uri = new URI("jms:SenderRequestQueue?deliveryMode=NON_PERSISTENT");
		try (WebServiceConnection connection = this.messageSender.createConnection(uri)) {
			SoapMessage soapRequest = new SaajSoapMessage(messageFactory.createMessage());
			soapRequest.setSoapAction(SOAP_ACTION);
			connection.send(soapRequest);

			this.testJmsListener.handleMessage((message) -> {
				assertNonEmptyByteMessage(message);
				return createEmptySoapMessage();
			});

			SoapMessage response = (SoapMessage) connection.receive(new SaajSoapMessageFactory(messageFactory));
			assertThat(response).isNotNull();
			assertThat(response.getSoapAction()).isEqualTo(SOAP_ACTION);
			assertThat(response.hasFault()).isFalse();
		}
	}

	@Test
	void testSendAndReceiveQueueBytesMessagePermanentQueue() throws Exception {
		String responseQueueName = "SenderResponseQueue";
		URI uri = new URI("jms:SenderRequestQueue?replyToName=" + responseQueueName + "&deliveryMode=NON_PERSISTENT");

		try (WebServiceConnection connection = this.messageSender.createConnection(uri)) {
			SoapMessage soapRequest = new SaajSoapMessage(messageFactory.createMessage());
			soapRequest.setSoapAction(SOAP_ACTION);
			connection.send(soapRequest);

			this.testJmsListener.handleMessage((message) -> {
				assertNonEmptyByteMessage(message);
				return createEmptySoapMessage();
			});
			waitForReply(responseQueueName);

			SoapMessage response = (SoapMessage) connection.receive(new SaajSoapMessageFactory(messageFactory));
			assertThat(response).isNotNull();
			assertThat(response.getSoapAction()).isEqualTo(SOAP_ACTION);
			assertThat(response.hasFault()).isFalse();
		}
	}

	@Test
	void testSendAndReceiveQueueTextMessage() throws Exception {
		URI uri = new URI("jms:SenderRequestQueue?deliveryMode=NON_PERSISTENT&messageType=TEXT_MESSAGE");
		try (WebServiceConnection connection = this.messageSender.createConnection(uri)) {
			SoapMessage soapRequest = new SaajSoapMessage(messageFactory.createMessage());
			soapRequest.setSoapAction(SOAP_ACTION);
			connection.send(soapRequest);

			this.testJmsListener.handleMessage((message) -> {
				assertNonEmptyTextMessage(message);
				return createEmptySoapMessage();
			});

			SoapMessage response = (SoapMessage) connection.receive(new SaajSoapMessageFactory(messageFactory));
			assertThat(response).isNotNull();
			assertThat(response.getSoapAction()).isEqualTo(SOAP_ACTION);
			assertThat(response.hasFault()).isFalse();
		}
	}

	@Test
	void testSendNoResponse() throws Exception {
		URI uri = new URI("jms:SenderRequestQueue?deliveryMode=NON_PERSISTENT");
		try (WebServiceConnection connection = this.messageSender.createConnection(uri)) {
			SoapMessage soapRequest = new SaajSoapMessage(messageFactory.createMessage());
			soapRequest.setSoapAction(SOAP_ACTION);
			connection.send(soapRequest);

			this.testJmsListener.handleMessage((message) -> {
				assertNonEmptyByteMessage(message);
				return null;
			});

			SoapMessage response = (SoapMessage) connection.receive(new SaajSoapMessageFactory(messageFactory));
			assertThat(response).isNull();
		}
	}

	@Test
	void testPostProcessor() throws Exception {
		MessagePostProcessor processor = message -> {
			message.setBooleanProperty("processed", true);
			return message;
		};
		URI uri = new URI("jms:SenderRequestQueue?deliveryMode=NON_PERSISTENT");
		try (JmsSenderConnection connection = (JmsSenderConnection) this.messageSender.createConnection(uri)) {
			connection.setPostProcessor(processor);
			SoapMessage soapRequest = new SaajSoapMessage(messageFactory.createMessage());
			connection.send(soapRequest);

			this.testJmsListener.handleMessage((message) -> {
				assertNonEmptyByteMessage(message);
				assertThat(message.getBooleanProperty("processed")).isTrue();
				return null;
			});
		}
	}

	private Object createEmptySoapMessage() throws SOAPException, IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		messageFactory.createMessage().writeTo(bos);
		String text = bos.toString(StandardCharsets.UTF_8);
		return MessageBuilder.withPayload(text)
			.setHeader(JmsTransportConstants.PROPERTY_SOAP_ACTION, SOAP_ACTION)
			.setHeader(JmsTransportConstants.PROPERTY_CONTENT_TYPE, SoapVersion.SOAP_11.getContentType())
			.build();
	}

	private static void assertNonEmptyByteMessage(jakarta.jms.Message message) throws JMSException {
		if (message instanceof BytesMessage bytesMessage) {
			assertThat(bytesMessage.readByte()).isNotEqualTo(-1);
		}
		else {
			throw new IllegalStateException("Unexpected message type: " + message.getClass().getName());
		}
	}

	private static void assertNonEmptyTextMessage(jakarta.jms.Message message) throws JMSException {
		if (message instanceof TextMessage textMessage) {
			assertThat(textMessage.getText()).isNotEmpty();
		}
		else {
			throw new IllegalStateException("Unexpected message type: " + message.getClass().getName());
		}
	}

	private static MessageFactory createMessageFactory() {
		try {
			return MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		}
		catch (SOAPException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private void waitForReply(String destinationName) {
		JmsTemplate jmsTemplate = new JmsTemplate(this.connectionFactory);
		Awaitility.await()
			.atMost(Duration.ofSeconds(3))
			.until(() -> jmsTemplate.browse(destinationName, new BrowserCallback<Boolean>() {
				@Override
				public Boolean doInJms(Session session, QueueBrowser browser) throws JMSException {
					return browser.getEnumeration().hasMoreElements();
				}
			}));
	}

	static class TestJmsListener {

		private ThrowingFunction<jakarta.jms.Message, Object> messageHandler;

		public void handleMessage(ThrowingFunction<Message, Object> messageHandler) {
			this.messageHandler = messageHandler;
		}

		@JmsListener(destination = "SenderRequestQueue")
		@SendTo("SenderResponseQueue")
		Object handleRequest(jakarta.jms.Message message) {
			return this.messageHandler.apply(message);
		}

	}

	@Configuration(proxyBeanMethods = false)
	@EnableJms
	@Import(TestJmsListener.class)
	static class JmsBrokerConfig {

		@Bean
		ActiveMQConnectionFactory connectionFactory() {
			return new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
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
			messageSender.setReceiveTimeout(Duration.ofSeconds(1).toMillis());
			return messageSender;
		}

	}

}
