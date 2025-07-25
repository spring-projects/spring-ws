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

package org.springframework.ws.test.client;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIOException;

class ExceptionResponseCreatorTests {

	@Test
	void ioException() {

		assertThatIOException().isThrownBy(() -> {

			ExceptionResponseCreator callback = new ExceptionResponseCreator(new IOException());
			callback.createResponse(null, null, null);
		});
	}

	@Test
	void runtimeException() {

		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {

			ExceptionResponseCreator callback = new ExceptionResponseCreator(new RuntimeException());
			callback.createResponse(null, null, null);
		});
	}

}
