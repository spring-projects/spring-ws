/*
 * Copyright 2005-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.test.server;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.SoapMessageDispatcher;

public class MockWebServiceClientTest {

	@Test
	public void createServerApplicationContext() {

		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.registerSingleton("messageDispatcher", SoapMessageDispatcher.class);
		applicationContext.registerSingleton("messageFactory", SaajSoapMessageFactory.class);
		applicationContext.refresh();

		MockWebServiceClient client = MockWebServiceClient.createClient(applicationContext);

		assertThat(client).isNotNull();
	}

	@Test
	public void createServerApplicationContextDefaults() {

		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.refresh();

		MockWebServiceClient client = MockWebServiceClient.createClient(applicationContext);

		assertThat(client).isNotNull();
	}
}
