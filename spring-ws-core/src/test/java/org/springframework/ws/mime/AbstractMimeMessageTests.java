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

package org.springframework.ws.mime;

import java.io.IOException;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.AbstractWebServiceMessageTests;
import org.springframework.ws.WebServiceMessage;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractMimeMessageTests extends AbstractWebServiceMessageTests {

	protected MimeMessage mimeMessage;

	private Resource picture;

	private String contentId;

	private String contentType;

	@Override
	protected final WebServiceMessage createWebServiceMessage() throws Exception {

		this.mimeMessage = createMimeMessage();
		this.picture = new ClassPathResource("spring-ws.png", AbstractMimeMessageTests.class);
		this.contentId = "spring-ws";
		this.contentType = "image/png";
		return this.mimeMessage;
	}

	protected abstract MimeMessage createMimeMessage() throws Exception;

	@Test
	public void testEmptyMessage() {

		Iterator<Attachment> iterator = this.mimeMessage.getAttachments();

		assertThat(iterator.hasNext()).isFalse();
	}

	@Test
	public void testAddAttachment() throws Exception {

		Attachment attachment = this.mimeMessage.addAttachment(this.contentId, this.picture, this.contentType);
		testAttachment(attachment);
	}

	@Test
	public void testGetAttachment() throws Exception {

		this.mimeMessage.addAttachment(this.contentId, this.picture, this.contentType);
		Attachment attachment = this.mimeMessage.getAttachment(this.contentId);

		assertThat(attachment).isNotNull();

		testAttachment(attachment);
	}

	@Test
	public void testGetAttachments() throws Exception {

		this.mimeMessage.addAttachment(this.contentId, this.picture, this.contentType);
		Iterator<Attachment> iterator = this.mimeMessage.getAttachments();

		assertThat(iterator).isNotNull();
		assertThat(iterator.hasNext()).isTrue();

		Attachment attachment = iterator.next();
		testAttachment(attachment);

		assertThat(iterator.hasNext()).isFalse();
	}

	private void testAttachment(Attachment attachment) throws IOException {

		assertThat(attachment.getContentId()).isEqualTo(this.contentId);
		assertThat(attachment.getContentType()).isEqualTo(this.contentType);
		assertThat(attachment.getSize()).isNotEqualTo(0);

		byte[] contents = FileCopyUtils.copyToByteArray(attachment.getInputStream());

		assertThat(contents.length).isGreaterThan(0);
	}

}
