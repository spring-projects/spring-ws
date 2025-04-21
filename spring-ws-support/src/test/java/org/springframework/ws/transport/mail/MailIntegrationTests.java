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

package org.springframework.ws.transport.mail;

import com.icegreen.greenmail.spring.GreenMailBean;
import com.icegreen.greenmail.util.GreenMailUtil;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(locations = "mail-applicationContext.xml")
class MailIntegrationTests {

	@Autowired
	private GreenMailBean greenMailBean;

	@Autowired
	private WebServiceTemplate webServiceTemplate;

	@Test
	void testMailTransport() {
		String content = """
				<root xmlns="http://springframework.org/spring-ws">
					<child/>
				</root>""";
		StringResult result = new StringResult();
		this.webServiceTemplate.sendSourceAndReceiveToResult(new StringSource(content), result);
		MimeMessage[] receivedMessages = this.greenMailBean.getGreenMail().getReceivedMessages();
		assertThat(receivedMessages).singleElement().satisfies((receivedMessage) -> {
			assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom()))
				.isEqualTo("Spring-WS SOAP Client <client@localhost>");
			assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("server@localhost");
			assertThat(GreenMailUtil.getBody(receivedMessage)).containsIgnoringWhitespaces(content);
		});
	}

}
