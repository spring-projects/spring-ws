/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.transport.mail;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.mock_javamail.Mailbox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.xmlunit.assertj.XmlAssert;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("mail-applicationContext.xml")
public class MailIntegrationTest {

	@Autowired private WebServiceTemplate webServiceTemplate;

	@AfterEach
	public void clearMailbox() {
		Mailbox.clearAll();
	}

	@Test
	public void testMailTransport() throws Exception {

		String content = "<root xmlns='http://springframework.org/spring-ws'><child/></root>";
		StringResult result = new StringResult();
		webServiceTemplate.sendSourceAndReceiveToResult(new StringSource(content), result);

		assertThat(Mailbox.get("server@example.com")).isEmpty();
		assertThat(Mailbox.get("client@example.com")).hasSize(1);
		XmlAssert.assertThat(result.toString()).and(content).ignoreWhitespace().areSimilar();
	}
}
