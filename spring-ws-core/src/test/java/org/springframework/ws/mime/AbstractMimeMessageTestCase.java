/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.mime;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.AbstractWebServiceMessageTestCase;
import org.springframework.ws.WebServiceMessage;

public abstract class AbstractMimeMessageTestCase extends AbstractWebServiceMessageTestCase {

	protected MimeMessage mimeMessage;

	private Resource picture;

	private String contentId;

	private String contentType;

	@Override
	protected final WebServiceMessage createWebServiceMessage() throws Exception {

		mimeMessage = createMimeMessage();
		picture = new ClassPathResource("spring-ws.png", AbstractMimeMessageTestCase.class);
		contentId = "spring-ws";
		contentType = "image/png";
		return mimeMessage;
	}

	protected abstract MimeMessage createMimeMessage() throws Exception;

	@Test
	public void testEmptyMessage() {

		Iterator<Attachment> iterator = mimeMessage.getAttachments();

		assertThat(iterator.hasNext()).isFalse();
	}

	@Test
	public void testAddAttachment() throws Exception {

		Attachment attachment = mimeMessage.addAttachment(contentId, picture, contentType);
		testAttachment(attachment);
	}

	@Test
	public void testGetAttachment() throws Exception {

		mimeMessage.addAttachment(contentId, picture, contentType);
		Attachment attachment = mimeMessage.getAttachment(contentId);

		assertThat(attachment).isNotNull();

		testAttachment(attachment);
	}

	@Test
	public void testGetAttachments() throws Exception {

		mimeMessage.addAttachment(contentId, picture, contentType);
		Iterator<Attachment> iterator = mimeMessage.getAttachments();

		assertThat(iterator).isNotNull();
		assertThat(iterator.hasNext()).isTrue();

		Attachment attachment = iterator.next();
		testAttachment(attachment);

		assertThat(iterator.hasNext()).isFalse();
	}

	private void testAttachment(Attachment attachment) throws IOException {

		assertThat(attachment.getContentId()).isEqualTo(contentId);
		assertThat(attachment.getContentType()).isEqualTo(contentType);
		assertThat(attachment.getSize()).isNotEqualTo(0);

		byte[] contents = FileCopyUtils.copyToByteArray(attachment.getInputStream());

		assertThat(contents.length).isGreaterThan(0);
	}

}
