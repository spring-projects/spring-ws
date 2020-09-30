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

package org.springframework.ws.transport.mail.support;

import java.net.URI;

import javax.mail.URLName;
import javax.mail.internet.InternetAddress;

import org.junit.Assert;
import org.junit.Test;

public class MailTransportUtilsTest {

	@Test
	public void testToPasswordProtectedString() throws Exception {
		URLName name = new URLName("imap://john:secret@imap.example.com/INBOX");
		String result = MailTransportUtils.toPasswordProtectedString(name);
		Assert.assertEquals("Password found in string", -1, result.indexOf("secret"));
	}

	@Test
	public void testGetTo() throws Exception {
		URI uri = new URI("mailto:infobot@example.com?subject=current-issue");
		InternetAddress to = MailTransportUtils.getTo(uri);
		Assert.assertEquals("Invalid destination", new InternetAddress("infobot@example.com"), to);

		uri = new URI("mailto:infobot@example.com");
		to = MailTransportUtils.getTo(uri);
		Assert.assertEquals("Invalid destination", new InternetAddress("infobot@example.com"), to);
	}

	@Test
	public void testGetSubject() throws Exception {
		URI uri = new URI("mailto:infobot@example.com?subject=current-issue");
		String subject = MailTransportUtils.getSubject(uri);
		Assert.assertEquals("Invalid destination", "current-issue", subject);

		uri = new URI("mailto:infobot@example.com");
		subject = MailTransportUtils.getSubject(uri);
		Assert.assertNull("Invalid destination", subject);
	}
}
