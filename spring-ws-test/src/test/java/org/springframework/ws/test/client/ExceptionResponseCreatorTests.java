/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
