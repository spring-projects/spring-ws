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
@ContextConfiguration("cglib-proxy-registration.xml")
class CgLibProxyRegistrationTests {

	@Autowired
	private PayloadRootAnnotationMethodEndpointMapping mapping;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void registration() throws NoSuchMethodException {

		MethodEndpoint cglibProxy = this.mapping
			.lookupEndpoint(new QName("http://springframework.org/spring-ws", "Request"));

		assertThat(cglibProxy).isNotNull();

		Method doIt = MyEndpoint.class.getMethod("doIt", Source.class);
		MethodEndpoint expected = new MethodEndpoint("cgLibProxyEndpoint", this.applicationContext, doIt);

		assertThat(cglibProxy).isEqualTo(expected);
	}

	@Endpoint
	public static class MyEndpoint {

		@PayloadRoot(localPart = "Request", namespace = "http://springframework.org/spring-ws")
		@Log
		public void doIt(@RequestPayload Source payload) {
		}

	}

}
