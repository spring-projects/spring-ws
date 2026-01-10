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

import java.util.List;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.server.endpoint.mapping.PayloadRootQNameEndpointMapping;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.SoapMessageDispatcher;
import org.springframework.ws.transport.test.EchoPayloadEndpoint;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import static org.xmlunit.assertj.XmlAssert.assertThat;

@SpringJUnitConfig
class JmsIntegrationTests {

	@Autowired
	private WebServiceTemplate webServiceTemplate;

	@Test
	void testTemporaryQueue() {
		String content = "<root xmlns='http://springframework.org/spring-ws'><child/></root>";
		StringResult result = new StringResult();
		this.webServiceTemplate.sendSourceAndReceiveToResult(new StringSource(content), result);
		assertThat(result.toString()).and(content).ignoreWhitespace().areSimilar();
	}

	@Test
	void testPermanentQueue() {
		String url = "jms:RequestQueue?deliveryMode=NON_PERSISTENT;replyToName=ResponseQueue";
		String content = "<root xmlns='http://springframework.org/spring-ws'><child/></root>";
		StringResult result = new StringResult();
		this.webServiceTemplate.sendSourceAndReceiveToResult(url, new StringSource(content), result);
		assertThat(result.toString()).and(content).ignoreWhitespace().areSimilar();
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
		SoapMessageDispatcher soapMessageDispatcher() {
			SoapMessageDispatcher messageDispatcher = new SoapMessageDispatcher();
			PayloadRootQNameEndpointMapping endpointMapping = new PayloadRootQNameEndpointMapping();
			endpointMapping.setDefaultEndpoint(new EchoPayloadEndpoint());
			messageDispatcher.setEndpointMappings(List.of(endpointMapping));
			return messageDispatcher;
		}

		@Bean
		WebServiceMessageListener webServiceMessageListener(WebServiceMessageFactory messageFactory,
				SoapMessageDispatcher soapMessageDispatcher) {
			WebServiceMessageListener webServiceMessageListener = new WebServiceMessageListener();
			webServiceMessageListener.setMessageFactory(messageFactory);
			webServiceMessageListener.setMessageReceiver(soapMessageDispatcher);
			return webServiceMessageListener;
		}

		@Bean
		DefaultMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory,
				WebServiceMessageListener messageListener) {
			DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
			container.setConnectionFactory(connectionFactory);
			container.setDestinationName("RequestQueue");
			container.setMessageListener(messageListener);
			return container;
		}

		@Bean
		WebServiceTemplate webServiceTemplate(WebServiceMessageFactory messageFactory,
				ConnectionFactory connectionFactory) {
			WebServiceTemplate webServiceTemplate = new WebServiceTemplate(messageFactory);
			JmsMessageSender jmsMessageSender = new JmsMessageSender();
			jmsMessageSender.setConnectionFactory(connectionFactory);
			webServiceTemplate.setMessageSender(jmsMessageSender);
			webServiceTemplate.setDefaultUri("jms:RequestQueue?deliveryMode=NON_PERSISTENT");
			return webServiceTemplate;
		}

	}

}
