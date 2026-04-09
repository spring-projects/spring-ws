package org.springframework.ws.transport.jms;

import java.time.Duration;

import io.micrometer.observation.tck.TestObservationRegistry;
import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
class WebServiceTemplateObservationIntegrationTests {

	private static final String REQUEST_QUEUE_NAME = "ClientRequestQueue";

	private static final String RESPONSE_QUEUE_NAME = "ClientResponseQueue";

	private static final String CONTENT = """
			<m:GetLastTradePrice xmlns:m='http://www.springframework.org/spring-ws'>
				<symbol>DIS</symbol>
			</m:GetLastTradePrice>""";

	@Autowired
	private WebServiceTemplate webServiceTemplate;

	@Autowired
	private TestObservationRegistry observationRegistry;

	@Test
	void sendMessageCreateClientObservation() {
		this.webServiceTemplate.sendSourceAndReceiveToResult(
				"jms:" + REQUEST_QUEUE_NAME + "?replyToName=" + RESPONSE_QUEUE_NAME + "&deliveryMode=NON_PERSISTENT",
				new org.springframework.xml.transform.StringSource(CONTENT),
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

	}

	static class TestJmsListener {

		@JmsListener(destination = REQUEST_QUEUE_NAME)
		@SendTo(RESPONSE_QUEUE_NAME)
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
