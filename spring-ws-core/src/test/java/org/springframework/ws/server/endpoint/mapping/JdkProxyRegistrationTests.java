/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.mapping;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("jdk-proxy-registration.xml")
class JdkProxyRegistrationTests {

	@Autowired
	private PayloadRootAnnotationMethodEndpointMapping mapping;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void registration() throws NoSuchMethodException {

		MethodEndpoint jdkProxy = this.mapping
			.lookupEndpoint(new QName("http://springframework.org/spring-ws", "Request"));

		assertThat(jdkProxy).isNotNull();

		Method doIt = MyEndpointImpl.class.getMethod("doIt", Source.class);
		MethodEndpoint expected = new MethodEndpoint("jdkProxyEndpoint", this.applicationContext, doIt);

		assertThat(jdkProxy).isEqualTo(expected);
	}

	@Endpoint
	public interface MyEndpoint {

		@PayloadRoot(localPart = "Request", namespace = "http://springframework.org/spring-ws")
		@Log
		void doIt(Source payload);

	}

	public static class MyEndpointImpl implements MyEndpoint {

		@Override
		public void doIt(@RequestPayload Source payload) {
		}

	}

}
