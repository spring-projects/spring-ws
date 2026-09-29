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

package org.springframework.ws.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import org.springframework.util.unit.DataSize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link BoundedInputStream}.
 *
 * @author Stephane Nicoll
 */
class BoundedInputStreamTests {

	@Test
	void createWithNegativeMaxSize() {
		assertThatIllegalArgumentException().isThrownBy(() -> createBoundedInputStream("test", -1))
			.withMessage("maxSize must be greater than 0");
	}

	@Test
	void createWithZeroMaxSize() {
		assertThatIllegalArgumentException().isThrownBy(() -> createBoundedInputStream("test", 0))
			.withMessage("maxSize must be greater than 0");
	}

	@Test
	void readUnderLimitSucceeds() throws IOException {
		String content = "Hello World";
		try (BoundedInputStream stream = createBoundedInputStream(content, 20)) {
			byte[] buffer = new byte[20];
			int bytesRead = stream.read(buffer);

			assertThat(bytesRead).isEqualTo(content.length());
			assertThat(new String(buffer, 0, bytesRead)).isEqualTo(content);
		}
	}

	@Test
	void readExactLimitSucceeds() throws IOException {
		String content = "Hello World";
		try (BoundedInputStream stream = createBoundedInputStream(content, content.length())) {
			byte[] buffer = new byte[content.length()];
			int bytesRead = stream.read(buffer);

			assertThat(bytesRead).isEqualTo(content.length());
			assertThat(stream.read()).isEqualTo(-1);
		}
	}

	@Test
	void readExceedingLimitByteByByteThrowsException() throws IOException {
		String content = "Hello World";
		try (BoundedInputStream stream = createBoundedInputStream(content, 5)) {
			for (int i = 0; i < 5; i++) {
				assertThat(stream.read()).isNotEqualTo(-1);
			}
			assertThatIOException().isThrownBy(stream::read).withMessage("Maximum message size exceeded: 5B");
		}
	}

	@Test
	void readExceedingLimitInBlockThrowsException() throws IOException {
		String content = "Hello World";
		try (BoundedInputStream stream = createBoundedInputStream(content, 7)) {
			byte[] buffer = new byte[3];
			stream.read(buffer);
			stream.read(buffer);
			assertThatIOException().isThrownBy(() -> stream.read(buffer))
				.withMessage("Maximum message size exceeded: 7B");
		}
	}

	@Test
	void skipExceedingLimitThrowsException() throws IOException {
		String content = "Hello World";
		try (BoundedInputStream stream = createBoundedInputStream(content, 5)) {
			assertThatIOException().isThrownBy(() -> stream.skip(6)).withMessage("Maximum message size exceeded: 5B");
		}
	}

	@Test
	void skipUnderLimitThenReadExceedingLimitThrowsException() throws IOException {
		String content = "Hello World";
		try (BoundedInputStream stream = createBoundedInputStream(content, 5)) {
			stream.skip(3);
			byte[] buffer = new byte[3];
			assertThatIOException().isThrownBy(() -> stream.read(buffer))
				.withMessage("Maximum message size exceeded: 5B");
		}
	}

	private BoundedInputStream createBoundedInputStream(String content, long maxBytes) {
		return new BoundedInputStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
				DataSize.ofBytes(maxBytes));
	}

}
