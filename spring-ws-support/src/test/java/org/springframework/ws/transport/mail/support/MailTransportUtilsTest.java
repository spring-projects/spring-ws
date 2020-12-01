/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.transport.mail.support;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;

import javax.mail.URLName;
import javax.mail.internet.InternetAddress;

import org.junit.jupiter.api.Test;

public class MailTransportUtilsTest {

	@Test
	public void testToPasswordProtectedString() {

		URLName name = new URLName("imap://john:secret@imap.example.com/INBOX");
		String result = MailTransportUtils.toPasswordProtectedString(name);

		assertThat(result.indexOf("secret")).isEqualTo(-1);
	}

	@Test
	public void testGetTo() throws Exception {

		URI uri = new URI("mailto:infobot@example.com?subject=current-issue");
		InternetAddress to = MailTransportUtils.getTo(uri);

		assertThat(to).isEqualTo(new InternetAddress("infobot@example.com"));

		uri = new URI("mailto:infobot@example.com");
		to = MailTransportUtils.getTo(uri);

		assertThat(to).isEqualTo(new InternetAddress("infobot@example.com"));
	}

	@Test
	public void testGetSubject() throws Exception {

		URI uri = new URI("mailto:infobot@example.com?subject=current-issue");
		String subject = MailTransportUtils.getSubject(uri);

		assertThat(subject).isEqualTo("current-issue");

		uri = new URI("mailto:infobot@example.com");
		subject = MailTransportUtils.getSubject(uri);

		assertThat(subject).isNull();
	}
}
