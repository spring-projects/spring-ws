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
import java.util.Map;
import java.util.Properties;

import com.icegreen.greenmail.spring.GreenMailBean;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.tck.TestObservationRegistry;
import jakarta.mail.Session;
import jakarta.mail.internet.AddressException;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.server.endpoint.mapping.PayloadRootQNameEndpointMapping;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.SoapMessageDispatcher;
import org.springframework.ws.transport.mail.monitor.PollingMonitoringStrategy;
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

	private static final String NAMESPACE = "http://springframework.org/spring-ws";

	// Use a dedicated mail server so that this test does not share an inbox with the
	// other tests of this package, which use the default 3000 port offset
	private static final int PORT_OFFSET = 3100;

	private static final int SMTP_PORT = 25 + PORT_OFFSET;

	private static final int IMAP_PORT = 143 + PORT_OFFSET;

	private static final String DEFAULT_URI = "mailto:system@localhost?subject=SOAP%20Test";

	private static final String STORE_URI = "imap://system:password@localhost:" + IMAP_PORT + "/INBOX";

	private static final String TRANSPORT_URI = "smtp://system:password@localhost:" + SMTP_PORT;

	private static final String PAYLOAD = """
			<root xmlns="%s">
			    <child/>
			</root>""".formatted(NAMESPACE);

	@Autowired
	private WebServiceTemplate webServiceTemplate;

	@Autowired
	private TestObservationRegistry observationRegistry;

	@Test
	void sendMailMessageCreatesClientAndServerObservations() {
		this.webServiceTemplate.sendSourceAndReceiveToResult(new StringSource(PAYLOAD), new StringResult());

		// The reply is polled from the mail store and may not have arrived yet, so the
		// outcome of the client exchange is either SUCCESS or UNKNOWN
		assertThat(this.observationRegistry).hasObservationWithNameEqualTo("soap.client.requests")
			.that()
			.hasBeenStopped()
			.doesNotHaveError()
			.hasContextualNameEqualTo("soap root")
			.hasLowCardinalityKeyValue("exception", "none")
			.hasLowCardinalityKeyValue("fault.code", "none")
			.hasLowCardinalityKeyValue("namespace", NAMESPACE)
			.hasLowCardinalityKeyValue("operation.name", "root")
			.hasLowCardinalityKeyValue("protocol", "mailto")
			.hasHighCardinalityKeyValue("fault.reason", "none")
			.hasHighCardinalityKeyValue("uri", DEFAULT_URI);

		// The server observation happens asynchronously as the receiver polls for emails
		await().untilAsserted(
				() -> assertThat(this.observationRegistry).hasObservationWithNameEqualTo("soap.server.requests")
					.that()
					.hasBeenStopped()
					.doesNotHaveError()
					.hasContextualNameEqualTo("soap root")
					.hasLowCardinalityKeyValue("exception", "none")
					.hasLowCardinalityKeyValue("fault.code", "none")
					.hasLowCardinalityKeyValue("namespace", NAMESPACE)
					.hasLowCardinalityKeyValue("operation.name", "root")
					.hasLowCardinalityKeyValue("outcome", "SUCCESS")
					.hasLowCardinalityKeyValue("protocol", "mailto")
					.hasHighCardinalityKeyValue("fault.reason", "none")
					.hasHighCardinalityKeyValue("uri", DEFAULT_URI));
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
			greenMailBean.setPortOffset(PORT_OFFSET);
			greenMailBean.setUsers(List.of("system:password@localhost"));
			return greenMailBean;
		}

		@Bean
		PayloadRootQNameEndpointMapping endpointMapping() {
			PayloadRootQNameEndpointMapping endpointMapping = new PayloadRootQNameEndpointMapping();
			endpointMapping.setEndpointMap(Map.of("{" + NAMESPACE + "}root", new EchoPayloadEndpoint()));
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
			messageReceiver.setStoreUri(STORE_URI);
			messageReceiver.setTransportUri(TRANSPORT_URI);
			messageReceiver.setMessageReceiver(messageDispatcher);
			messageReceiver.setSession(session);
			messageReceiver.setObservationRegistry(observationRegistry);
			PollingMonitoringStrategy monitoringStrategy = new PollingMonitoringStrategy();
			monitoringStrategy.setPollingInterval(500);
			messageReceiver.setMonitoringStrategy(monitoringStrategy);
			return messageReceiver;
		}

		@Bean
		WebServiceTemplate webServiceTemplate(WebServiceMessageFactory messageFactory, Session session,
				ObservationRegistry observationRegistry) throws AddressException {
			WebServiceTemplate webServiceTemplate = new WebServiceTemplate(messageFactory);
			MailMessageSender messageSender = new MailMessageSender();
			messageSender.setFrom("Spring-WS SOAP Client <client@localhost>");
			messageSender.setTransportUri(TRANSPORT_URI);
			messageSender.setStoreUri(STORE_URI);
			messageSender.setReceiveSleepTime(1000);
			messageSender.setSession(session);
			webServiceTemplate.setMessageSender(messageSender);
			webServiceTemplate.setDefaultUri(DEFAULT_URI);
			webServiceTemplate.setObservationRegistry(observationRegistry);
			return webServiceTemplate;
		}

	}

}
