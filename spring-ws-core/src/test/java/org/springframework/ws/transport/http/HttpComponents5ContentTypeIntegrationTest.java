/*
 * Copyright 2005-2025 the original author or authors.
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
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class HttpComponents5ContentTypeIntegrationTest
		extends AbstractHttpWebServiceMessageSenderIntegrationTest<HttpComponents5MessageSender> {

	@Override
	protected HttpComponents5MessageSender createMessageSender() {

		ExecChainHandler testHandler = (request, scope, chain) -> {
			assertThat(request.getEntity().getContentType()).isNotBlank();
			return chain.proceed(request, scope);
		};

		HttpClient client = HttpClientBuilder.create() //
			.addRequestInterceptorFirst(new HttpComponents5MessageSender.RemoveSoapHeadersInterceptor()) //
			.addExecInterceptorFirst("handler with assertion", testHandler) //
			.build();

		return new HttpComponents5MessageSender(client);
	}

}
