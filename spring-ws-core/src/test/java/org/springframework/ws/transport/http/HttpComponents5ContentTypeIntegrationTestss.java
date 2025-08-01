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
