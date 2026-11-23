/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.mail.support;

import java.net.URI;

import jakarta.mail.URLName;
import jakarta.mail.internet.InternetAddress;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MailTransportUtilsTests {

	@Test
	void testToPasswordProtectedString() {

		URLName name = new URLName("imap://john:secret@imap.example.com/INBOX");
		String result = MailTransportUtils.toPasswordProtectedString(name);

		assertThat(result.indexOf("secret")).isEqualTo(-1);
	}

	@Test
	void testGetTo() throws Exception {

		URI uri = new URI("mailto:infobot@example.com?subject=current-issue");
		InternetAddress to = MailTransportUtils.getTo(uri);

		assertThat(to).isEqualTo(new InternetAddress("infobot@example.com"));

		uri = new URI("mailto:infobot@example.com");
		to = MailTransportUtils.getTo(uri);

		assertThat(to).isEqualTo(new InternetAddress("infobot@example.com"));
	}

	@Test
	void testGetSubject() throws Exception {

		URI uri = new URI("mailto:infobot@example.com?subject=current-issue");
		String subject = MailTransportUtils.getSubject(uri);

		assertThat(subject).isEqualTo("current-issue");

		uri = new URI("mailto:infobot@example.com");
		subject = MailTransportUtils.getSubject(uri);

		assertThat(subject).isNull();
	}

}
