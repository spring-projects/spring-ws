/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.jms;

import static org.assertj.core.api.Assertions.*;

import jakarta.jms.BytesMessage;
import jakarta.jms.TextMessage;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConstants;

import java.io.ByteArrayOutputStream;
import java.net.URI;
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
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.WebServiceConnection;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("jms-sender-applicationContext.xml")
public class JmsMessageSenderIntegrationTest {

	@Autowired private JmsMessageSender messageSender;

	@Autowired private JmsTemplate jmsTemplate;

	private MessageFactory messageFactory;

	private static final String SOAP_ACTION = "\"http://springframework.org/DoIt\"";

	private EmbeddedActiveMQ server;

	@BeforeEach
	public void createMessageFactory() throws Exception {
		messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);

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
	public void testSendAndReceiveQueueBytesMessageTemporaryQueue() throws Exception {

		URI uri = new URI("jms:SenderRequestQueue?deliveryMode=NON_PERSISTENT");

		try (WebServiceConnection connection = messageSender.createConnection(uri)) {

			SoapMessage soapRequest = new SaajSoapMessage(messageFactory.createMessage());
			soapRequest.setSoapAction(SOAP_ACTION);
			connection.send(soapRequest);

			BytesMessage request = (BytesMessage) jmsTemplate.receive();

			assertThat(request).isNotNull();
			assertThat(request.readByte()).isNotEqualTo(-1);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			messageFactory.createMessage().writeTo(bos);
			final byte[] buf = bos.toByteArray();

			jmsTemplate.send(request.getJMSReplyTo(), session -> {

				BytesMessage response = session.createBytesMessage();
				response.setStringProperty(JmsTransportConstants.PROPERTY_SOAP_ACTION, SOAP_ACTION);
				response.setStringProperty(JmsTransportConstants.PROPERTY_CONTENT_TYPE, SoapVersion.SOAP_11.getContentType());
				response.writeBytes(buf);
				return response;
			});

			SoapMessage response = (SoapMessage) connection.receive(new SaajSoapMessageFactory(messageFactory));

			assertThat(response).isNotNull();
			assertThat(response.getSoapAction()).isEqualTo(SOAP_ACTION);
			assertThat(response.hasFault()).isFalse();
		}
	}

	@Test
	public void testSendAndReceiveQueueBytesMessagePermanentQueue() throws Exception {

		String responseQueueName = "SenderResponseQueue";
		URI uri = new URI("jms:SenderRequestQueue?replyToName=" + responseQueueName + "&deliveryMode=NON_PERSISTENT");

		try (WebServiceConnection connection = messageSender.createConnection(uri)) {

			SoapMessage soapRequest = new SaajSoapMessage(messageFactory.createMessage());
			soapRequest.setSoapAction(SOAP_ACTION);
			connection.send(soapRequest);

			final BytesMessage request = (BytesMessage) jmsTemplate.receive();

			assertThat(request).isNotNull();
			assertThat(request.readByte()).isNotEqualTo(-1);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			messageFactory.createMessage().writeTo(bos);
			final byte[] buf = bos.toByteArray();

			jmsTemplate.send(responseQueueName, session -> {

				BytesMessage response = session.createBytesMessage();
				response.setJMSCorrelationID(request.getJMSMessageID());
				response.setStringProperty(JmsTransportConstants.PROPERTY_SOAP_ACTION, SOAP_ACTION);
				response.setStringProperty(JmsTransportConstants.PROPERTY_CONTENT_TYPE, SoapVersion.SOAP_11.getContentType());
				response.writeBytes(buf);
				return response;
			});

			SoapMessage response = (SoapMessage) connection.receive(new SaajSoapMessageFactory(messageFactory));

			assertThat(response).isNotNull();
			assertThat(response.getSoapAction()).isEqualTo(SOAP_ACTION);
			assertThat(response.hasFault()).isFalse();
		}
	}

	@Test
	public void testSendAndReceiveQueueTextMessage() throws Exception {

		URI uri = new URI("jms:SenderRequestQueue?deliveryMode=NON_PERSISTENT&messageType=TEXT_MESSAGE");

		try (WebServiceConnection connection = messageSender.createConnection(uri)) {

			SoapMessage soapRequest = new SaajSoapMessage(messageFactory.createMessage());
			soapRequest.setSoapAction(SOAP_ACTION);
			connection.send(soapRequest);

			TextMessage request = (TextMessage) jmsTemplate.receive();

			assertThat(request).isNotNull();
			assertThat(request.getText()).isNotNull();

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			messageFactory.createMessage().writeTo(bos);
			final String text = new String(bos.toByteArray(), StandardCharsets.UTF_8);

			jmsTemplate.send(request.getJMSReplyTo(), session -> {

				TextMessage response = session.createTextMessage();
				response.setStringProperty(JmsTransportConstants.PROPERTY_SOAP_ACTION, SOAP_ACTION);
				response.setStringProperty(JmsTransportConstants.PROPERTY_CONTENT_TYPE, SoapVersion.SOAP_11.getContentType());
				response.setText(text);
				return response;
			});

			SoapMessage response = (SoapMessage) connection.receive(new SaajSoapMessageFactory(messageFactory));

			assertThat(response).isNotNull();
			assertThat(response.getSoapAction()).isEqualTo(SOAP_ACTION);
			assertThat(response.hasFault()).isFalse();
		}
	}

	@Test
	public void testSendNoResponse() throws Exception {

		URI uri = new URI("jms:SenderRequestQueue?deliveryMode=NON_PERSISTENT");

		try (WebServiceConnection connection = messageSender.createConnection(uri)) {

			SoapMessage soapRequest = new SaajSoapMessage(messageFactory.createMessage());
			soapRequest.setSoapAction(SOAP_ACTION);
			connection.send(soapRequest);

			BytesMessage request = (BytesMessage) jmsTemplate.receive();

			assertThat(request).isNotNull();

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			messageFactory.createMessage().writeTo(bos);
			SoapMessage response = (SoapMessage) connection.receive(new SaajSoapMessageFactory(messageFactory));

			assertThat(response).isNull();
		}
	}

	@Test
	public void testPostProcessor() throws Exception {

		MessagePostProcessor processor = message -> {

			message.setBooleanProperty("processed", true);
			return message;
		};

		URI uri = new URI("jms:SenderRequestQueue?deliveryMode=NON_PERSISTENT");

		try (JmsSenderConnection connection = (JmsSenderConnection) messageSender.createConnection(uri)) {

			connection.setPostProcessor(processor);
			SoapMessage soapRequest = new SaajSoapMessage(messageFactory.createMessage());
			connection.send(soapRequest);

			BytesMessage request = (BytesMessage) jmsTemplate.receive();

			assertThat(request).isNotNull();
			assertThat(request.getBooleanProperty("processed")).isTrue();
		}
	}
}
