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

package org.springframework.ws.test.server.integration;

import static org.springframework.ws.test.server.RequestCreators.*;
import static org.springframework.ws.test.server.ResponseMatchers.*;

import javax.xml.transform.Source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ws.test.server.MockWebServiceClient;
import org.springframework.xml.transform.StringSource;

/**
 * @author Arjen Poutsma
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("integration-test.xml")
public class ServerIntegrationTest {

	@Autowired private ApplicationContext applicationContext;

	private MockWebServiceClient mockClient;

	@BeforeEach
	public void createClient() {
		mockClient = MockWebServiceClient.createClient(applicationContext);
	}

	@Test
	public void basic() {

		Source requestPayload = new StringSource("<customerCountRequest xmlns='http://springframework.org/spring-ws'>"
				+ "<customerName>John Doe</customerName>" + "</customerCountRequest>");
		Source expectedResponsePayload = new StringSource(
				"<customerCountResponse xmlns='http://springframework.org/spring-ws'>" + "<customerCount>42</customerCount>"
						+ "</customerCountResponse>");

		mockClient.sendRequest(withPayload(requestPayload)).andExpect(payload(expectedResponsePayload));
	}
}
