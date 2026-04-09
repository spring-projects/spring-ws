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

package org.springframework.ws.transport.mail;

import java.util.List;
import java.util.Properties;

import com.icegreen.greenmail.spring.GreenMailBean;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.tck.TestObservationRegistry;
import jakarta.mail.Session;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.server.endpoint.mapping.PayloadRootQNameEndpointMapping;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.SoapMessageDispatcher;
import org.springframework.ws.transport.mail.monitor.Pop3PollingMonitoringStrategy;
import org.springframework.ws.transport.test.EchoPayloadEndpoint;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Tests for Mail transport with observations.
 *
 * @author Stephane Nicoll
 */
@SpringJUnitConfig
@DirtiesContext
class MailObservationIntegrationTests {

	@Autowired
	private WebServiceTemplate webServiceTemplate;

	@Autowired
	private GreenMailBean greenMailBean;

	@Autowired
	private TestObservationRegistry observationRegistry;

	@Test
	void sendMailMessageCreateObservations() {
		String content = """
				<root xmlns="http://springframework.org/spring-ws">
					<child/>
				</root>""";
		StringResult result = new StringResult();
		this.webServiceTemplate.sendSourceAndReceiveToResult(new StringSource(content), result);

		assertThat(this.observationRegistry).hasObservationWithNameEqualTo("soap.client.requests")
			.that()
			.hasBeenStopped()
			.doesNotHaveError()
			.hasLowCardinalityKeyValue("fault.code", "none")
			.hasLowCardinalityKeyValue("protocol", "mailto")
			.hasLowCardinalityKeyValue("namespace", "http://springframework.org/spring-ws")
			.hasLowCardinalityKeyValue("operation.name", "root")
			.hasHighCardinalityKeyValue("fault.reason", "none")
			.hasHighCardinalityKeyValue("uri", "mailto:system@localhost?subject=SOAP%20Test");

		// Mail server observation happens asynchronously as the receiver polls for emails
		await().untilAsserted(
				() -> assertThat(this.observationRegistry).hasObservationWithNameEqualTo("soap.server.requests")
					.that()
					.hasBeenStopped()
					.doesNotHaveError()
					.hasLowCardinalityKeyValue("fault.code", "none")
					.hasLowCardinalityKeyValue("protocol", "mailto")
					.hasLowCardinalityKeyValue("namespace", "http://springframework.org/spring-ws")
					.hasLowCardinalityKeyValue("operation.name", "root")
					.hasHighCardinalityKeyValue("fault.reason", "none")
					.hasHighCardinalityKeyValue("uri", "mailto:system@localhost?subject=SOAP%20Test"));
	}

	@Configuration(proxyBeanMethods = false)
	static class Config {

		@Bean
		SaajSoapMessageFactory messageFactory() {
			return new SaajSoapMessageFactory();
		}

		@Bean
		TestObservationRegistry observationRegistry() {
			return TestObservationRegistry.create();
		}

		@Bean
		GreenMailBean mailServer() {
			GreenMailBean greenMailBean = new GreenMailBean();
			greenMailBean.setImapProtocol(true);
			greenMailBean.setUsers(List.of("system:password@localhost"));
			return greenMailBean;
		}

		@Bean
		PayloadRootQNameEndpointMapping endpointMapping() {
			PayloadRootQNameEndpointMapping endpointMapping = new PayloadRootQNameEndpointMapping();
			endpointMapping.setDefaultEndpoint(new EchoPayloadEndpoint());
			return endpointMapping;
		}

		@Bean
		SoapMessageDispatcher messageDispatcher(EndpointMapping endpointMapping) {
			SoapMessageDispatcher messageDispatcher = new SoapMessageDispatcher();
			messageDispatcher.setEndpointMappings(List.of(endpointMapping));
			return messageDispatcher;
		}

		@Bean
		Session mailSession() {
			return Session.getInstance(new Properties());
		}

		@Bean
		@DependsOn("mailServer")
		MailMessageReceiver messageReceiver(WebServiceMessageFactory messageFactory,
				SoapMessageDispatcher messageDispatcher, Session session, ObservationRegistry observationRegistry)
				throws AddressException {
			MailMessageReceiver messageReceiver = new MailMessageReceiver();
			messageReceiver.setMessageFactory(messageFactory);
			messageReceiver.setFrom("Spring-WS SOAP Server <server@localhost>");
			messageReceiver.setStoreUri("imap://system:password@localhost:3143/INBOX");
			messageReceiver.setTransportUri("smtp://system:password@localhost:3025");
			messageReceiver.setMessageReceiver(messageDispatcher);
			messageReceiver.setSession(session);
			messageReceiver.setObservationRegistry(observationRegistry);
			org.springframework.ws.transport.mail.monitor.PollingMonitoringStrategy monitoringStrategy = new org.springframework.ws.transport.mail.monitor.PollingMonitoringStrategy();
			monitoringStrategy.setPollingInterval(500);
			messageReceiver.setMonitoringStrategy(monitoringStrategy);
			return messageReceiver;
		}

		@Bean
		WebServiceTemplate webServiceTemplate(WebServiceMessageFactory messageFactory, Session session,
				TestObservationRegistry observationRegistry) throws AddressException {
			WebServiceTemplate webServiceTemplate = new WebServiceTemplate(messageFactory);
			MailMessageSender messageSender = new MailMessageSender();
			messageSender.setFrom("Spring-WS SOAP Client <client@localhost>");
			messageSender.setTransportUri("smtp://system:password@localhost:3025");
			messageSender.setStoreUri("imap://system:password@localhost:3143/INBOX");
			messageSender.setReceiveSleepTime(1000);
			messageSender.setSession(session);
			webServiceTemplate.setMessageSender(messageSender);
			webServiceTemplate.setDefaultUri("mailto:system@localhost?subject=SOAP%20Test");
			webServiceTemplate.setObservationRegistry(observationRegistry);
			return webServiceTemplate;
		}

	}

}
