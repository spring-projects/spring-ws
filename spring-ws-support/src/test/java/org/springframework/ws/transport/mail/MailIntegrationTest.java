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

package org.springframework.ws.transport.mail;

import static org.assertj.core.api.Assertions.*;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import com.icegreen.greenmail.spring.GreenMailBean;
import com.icegreen.greenmail.util.GreenMailUtil;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("mail-applicationContext.xml")
public class MailIntegrationTest {

	@Autowired private GreenMailBean greenMailBean;

	@Autowired private WebServiceTemplate webServiceTemplate;

	@Disabled("doesn't run under Spring Framework 6.0.1-SNAPSHOT")
    @Test
	public void testMailTransport() throws MessagingException {

		String content = "<root xmlns=\"http://springframework.org/spring-ws\"><child/></root>";
		StringResult result = new StringResult();
		webServiceTemplate.sendSourceAndReceiveToResult(new StringSource(content), result);

		MimeMessage[] receivedMessages = greenMailBean.getGreenMail().getReceivedMessages();

		assertThat(receivedMessages).hasSize(1);

		assertThat(GreenMailUtil.getAddressList(receivedMessages[0].getFrom()))
				.isEqualTo("Spring-WS SOAP Client <client@localhost>");
		assertThat(GreenMailUtil.getAddressList(receivedMessages[0].getAllRecipients())).isEqualTo("server@localhost");
		assertThat(GreenMailUtil.getBody(receivedMessages[0])).contains(content);
	}
}
