/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.http;

class ClientHttpRequestMessageSenderIntegrationTests
		extends AbstractHttpWebServiceMessageSenderIntegrationTests<ClientHttpRequestMessageSender> {

	@Override
	protected ClientHttpRequestMessageSender createMessageSender() {
		return new ClientHttpRequestMessageSender();
	}

}
