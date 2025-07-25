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

package org.springframework.ws.server.endpoint.mapping;

import java.lang.reflect.Method;
import java.util.Collections;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPMessage;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.MessageDispatcher;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.PayloadRoots;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.SoapMessageDispatcher;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("payloadRootAnnotationMethodEndpointMapping.xml")
class PayloadRootAnnotationMethodEndpointMappingTests {

	@Autowired
	private PayloadRootAnnotationMethodEndpointMapping mapping;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void registrationSingle() throws NoSuchMethodException {

		MethodEndpoint endpoint = this.mapping
			.lookupEndpoint(new QName("http://springframework.org/spring-ws", "Request"));

		assertThat(endpoint).isNotNull();

		Method doIt = MyEndpoint.class.getMethod("doIt", Source.class);
		MethodEndpoint expected = new MethodEndpoint("endpoint", this.applicationContext, doIt);

		assertThat(endpoint).isEqualTo(expected);
	}

	@Test
	void registrationMultiple() throws NoSuchMethodException {

		Method doItMultiple = MyEndpoint.class.getMethod("doItMultiple");
		MethodEndpoint expected = new MethodEndpoint("endpoint", this.applicationContext, doItMultiple);

		MethodEndpoint endpoint = this.mapping
			.lookupEndpoint(new QName("http://springframework.org/spring-ws", "Request1"));

		assertThat(endpoint).isNotNull();
		assertThat(endpoint).isEqualTo(expected);

		endpoint = this.mapping.lookupEndpoint(new QName("http://springframework.org/spring-ws", "Request2"));

		assertThat(endpoint).isNotNull();
		assertThat(endpoint).isEqualTo(expected);
	}

	@Test
	void registrationRepeatable() throws NoSuchMethodException {

		Method doItMultiple = MyEndpoint.class.getMethod("doItRepeatable");
		MethodEndpoint expected = new MethodEndpoint("endpoint", this.applicationContext, doItMultiple);

		MethodEndpoint endpoint = this.mapping
			.lookupEndpoint(new QName("http://springframework.org/spring-ws", "Request3"));

		assertThat(endpoint).isNotNull();
		assertThat(endpoint).isEqualTo(expected);

		endpoint = this.mapping.lookupEndpoint(new QName("http://springframework.org/spring-ws", "Request4"));

		assertThat(endpoint).isNotNull();
		assertThat(endpoint).isEqualTo(expected);
	}

	@Test
	void registrationInvalid() {
		assertThat(this.mapping.lookupEndpoint(new QName("http://springframework.org/spring-ws", "Invalid"))).isNull();
	}

	@Test
	void invoke() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage request = messageFactory.createMessage();
		request.getSOAPBody().addBodyElement(QName.valueOf("{http://springframework.org/spring-ws}Request"));
		MessageContext messageContext = new DefaultMessageContext(new SaajSoapMessage(request),
				new SaajSoapMessageFactory(messageFactory));
		DefaultMethodEndpointAdapter adapter = new DefaultMethodEndpointAdapter();
		adapter.afterPropertiesSet();

		MessageDispatcher messageDispatcher = new SoapMessageDispatcher();
		messageDispatcher.setApplicationContext(this.applicationContext);
		messageDispatcher.setEndpointMappings(Collections.singletonList(this.mapping));
		messageDispatcher.setEndpointAdapters(Collections.singletonList(adapter));

		messageDispatcher.receive(messageContext);

		MyEndpoint endpoint = this.applicationContext.getBean("endpoint", MyEndpoint.class);
		assertThat(endpoint.isDoItInvoked()).isTrue();

		LogAspect aspect = (LogAspect) this.applicationContext.getBean("logAspect");
		assertThat(aspect.isLogInvoked()).isTrue();
	}

	@Endpoint
	public static class MyEndpoint {

		private static final org.apache.commons.logging.Log logger = LogFactory.getLog(MyEndpoint.class);

		private boolean doItInvoked = false;

		public boolean isDoItInvoked() {
			return this.doItInvoked;
		}

		@PayloadRoot(localPart = "Request", namespace = "http://springframework.org/spring-ws")
		@Log
		public void doIt(@RequestPayload Source payload) {

			this.doItInvoked = true;
			logger.info("In doIt()");
		}

		@PayloadRoots({ @PayloadRoot(localPart = "Request1", namespace = "http://springframework.org/spring-ws"),
				@PayloadRoot(localPart = "Request2", namespace = "http://springframework.org/spring-ws") })
		public void doItMultiple() {
		}

		@PayloadRoot(localPart = "Request3", namespace = "http://springframework.org/spring-ws")
		@PayloadRoot(localPart = "Request4", namespace = "http://springframework.org/spring-ws")
		public void doItRepeatable() {

		}

	}

	static class OtherBean {

		@PayloadRoot(localPart = "Invalid", namespace = "http://springframework.org/spring-ws")
		public void doIt() {

		}

	}

}
