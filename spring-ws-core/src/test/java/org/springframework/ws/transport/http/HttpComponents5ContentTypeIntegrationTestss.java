/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.http;

import org.apache.hc.client5.http.classic.ExecChainHandler;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class HttpComponents5ContentTypeIntegrationTests
		extends AbstractHttpWebServiceMessageSenderIntegrationTests<AbstractHttpComponents5MessageSender> {

	@Override
	protected AbstractHttpComponents5MessageSender createMessageSender() {
		ExecChainHandler testHandler = (request, scope, chain) -> {
			assertThat(request.getEntity().getContentType()).isNotBlank();
			return chain.proceed(request, scope);
		};
		HttpComponents5ClientFactory factory = HttpComponents5ClientFactory.withDefaults();
		factory.addClientBuilderCustomizer(
				builder -> builder.addExecInterceptorFirst("handler with assertion", testHandler));
		return new SimpleHttpComponents5MessageSender(factory.build());
	}

}
