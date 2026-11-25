/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.context;

import java.lang.reflect.Method;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TransportContextHolder}.
 *
 * @author Seunghyeon An
 */
class TransportContextHolderTests {

	@Test
	void returnsNullWhenNoContextIsBound() {
		TransportContext previousContext = TransportContextHolder.getTransportContext();
		try {
			TransportContextHolder.setTransportContext(null);
			assertThat(TransportContextHolder.getTransportContext()).isNull();
		}
		finally {
			TransportContextHolder.setTransportContext(previousContext);
		}
	}

	@Test
	void declaresNullableReturnType() throws NoSuchMethodException {
		Method getter = TransportContextHolder.class.getDeclaredMethod("getTransportContext");

		assertThat(getter.getAnnotatedReturnType().getAnnotation(Nullable.class)).isNotNull();
	}

}
