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

package org.springframework.ws.transport.mail;

import java.net.URI;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import com.icegreen.greenmail.spring.GreenMailBean;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Integration tests for {@link MailSenderConnection}.
 *
 * @author Stephane Nicoll
 */
class MailSenderConnectionIntegrationTests {

	private static final String SOAP_ACTION = "https://ws.example.com/DoIt";

	private static final Pattern MESSAGE_ID = Pattern.compile("^<(.+)@(.+)>$");

	private GreenMailBean greenMailBean;

	private MailMessageSender messageSender;

	private MessageFactory messageFactory;

	private Session session;

	@BeforeEach
	void setUp() throws Exception {
		this.greenMailBean = new GreenMailBean();
		this.greenMailBean.setAutostart(true);
		this.greenMailBean.setSmtpProtocol(true);
		this.greenMailBean.setImapProtocol(true);
		this.greenMailBean.setPortOffset(1000);
		this.greenMailBean.setUsers(Collections.singletonList("client:password@localhost"));
		this.greenMailBean.afterPropertiesSet();

		this.session = Session.getInstance(new Properties());
		this.messageSender = new MailMessageSender();
		this.messageSender.setFrom("Spring-WS SOAP Client <client@localhost>");
		this.messageSender.setTransportUri("smtp://localhost:" + this.greenMailBean.getGreenMail().getSmtp().getPort());
		this.messageSender.setStoreUri(
				"imap://client:password@localhost:" + this.greenMailBean.getGreenMail().getImap().getPort() + "/INBOX");
		this.messageSender.setReceiveSleepTime(0);
		this.messageSender.setSession(this.session);
		this.messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		this.messageSender.afterPropertiesSet();
	}

	@AfterEach
	void tearDown() throws Exception {
		this.greenMailBean.destroy();
	}

	@Test
	void requestMessageIdIsUnguessable() throws Exception {
		String firstId = messageId(sendRequest("server@localhost"));
		String secondId = messageId(sendRequest("server@localhost"));

		assertThat(firstId).satisfies(localPartIsUUID());
		assertThat(secondId).satisfies(localPartIsUUID());
		assertThat(firstId).isNotEqualTo(secondId);
	}

	@Test
	void forgedResponseFromUnexpectedSenderIsRejectedWhenVerifying() throws Exception {
		this.messageSender.setVerifyResponseSender(true);
		MailSenderConnection connection = sendRequest("server@localhost");
		deliverReply("attacker@evil.example.com", messageId(connection));

		connection.onReceiveBeforeRead();
		assertThat(connection.hasResponse()).isFalse();
	}

	@Test
	void responseFromExpectedSenderIsAcceptedWhenVerifying() throws Exception {
		this.messageSender.setVerifyResponseSender(true);
		MailSenderConnection connection = sendRequest("server@localhost");
		deliverReply("server@localhost", messageId(connection));

		connection.onReceiveBeforeRead();
		assertThat(connection.hasResponse()).isTrue();
	}

	@Test
	void responseIsCorrelatedByMessageIdWithoutSenderVerification() throws Exception {
		MailSenderConnection connection = sendRequest("server@localhost");
		deliverReply("server@localhost", messageId(connection));

		connection.onReceiveBeforeRead();
		assertThat(connection.hasResponse()).isTrue();
	}

	private MailSenderConnection sendRequest(String to) throws Exception {
		URI mailTo = new URI("mailto:" + to + "?subject=SOAP%20Test");
		MailSenderConnection connection = (MailSenderConnection) this.messageSender.createConnection(mailTo);
		SOAPMessage saajMessage = this.messageFactory.createMessage();
		saajMessage.getSOAPBody().addBodyElement(new QName("https://ws.example.com", "test"));
		SoapMessage soapRequest = new SaajSoapMessage(saajMessage);
		soapRequest.setSoapAction(SOAP_ACTION);
		connection.send(soapRequest);
		return connection;
	}

	private void deliverReply(String from, String inReplyTo) throws Exception {
		MimeMessage reply = new MimeMessage(this.session);
		reply.setFrom(new InternetAddress(from));
		reply.setRecipient(RecipientType.TO, new InternetAddress("client@localhost"));
		reply.setHeader(MailTransportConstants.HEADER_IN_REPLY_TO, inReplyTo);
		reply.setText("<Envelope/>");
		reply.saveChanges();
		try (Transport transport = this.session.getTransport("smtp")) {
			transport.connect("localhost", this.greenMailBean.getGreenMail().getSmtp().getPort(), null, null);
			transport.sendMessage(reply, reply.getAllRecipients());
		}
	}

	private static String messageId(MailSenderConnection connection) throws Exception {
		return connection.getRequestMessage().getMessageID();
	}

	private static Consumer<String> localPartIsUUID() {
		return (messageId) -> {
			Matcher matcher = MESSAGE_ID.matcher(messageId);
			assertThat(matcher.matches()).as("Message-ID [%s] is well-formed", messageId).isTrue();
			String value = matcher.group(1);
			assertThatNoException().as("local part [%s] is a random UUID", value)
				.isThrownBy(() -> UUID.fromString(value));
		};
	}

}
