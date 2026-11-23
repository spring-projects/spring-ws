/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.http;

class JdkHttpClientMessageSenderIntegrationTests
		extends AbstractHttpWebServiceMessageSenderIntegrationTests<JdkHttpClientMessageSender> {

	@Override
	protected JdkHttpClientMessageSender createMessageSender() {
		return new JdkHttpClientMessageSender();
	}

}
