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

package org.springframework.ws.transport.jms;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import static org.xmlunit.assertj.XmlAssert.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("jms-applicationContext.xml")
class JmsIntegrationTests {

	@Autowired
	private WebServiceTemplate webServiceTemplate;

	@Test
	void testTemporaryQueue() {
		String content = "<root xmlns='http://springframework.org/spring-ws'><child/></root>";
		StringResult result = new StringResult();
		this.webServiceTemplate.sendSourceAndReceiveToResult(new StringSource(content), result);
		assertThat(result.toString()).and(content).ignoreWhitespace().areSimilar();
	}

	@Test
	void testPermanentQueue() {
		String url = "jms:RequestQueue?deliveryMode=NON_PERSISTENT;replyToName=ResponseQueue";
		String content = "<root xmlns='http://springframework.org/spring-ws'><child/></root>";
		StringResult result = new StringResult();
		this.webServiceTemplate.sendSourceAndReceiveToResult(url, new StringSource(content), result);
		assertThat(result.toString()).and(content).ignoreWhitespace().areSimilar();
	}

}
