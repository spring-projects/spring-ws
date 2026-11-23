/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractWebServiceMessageFactoryTests {

	protected WebServiceMessageFactory messageFactory;

	@BeforeEach
	public final void setUp() throws Exception {
		this.messageFactory = createMessageFactory();
	}

	@Test
	protected void testCreateEmptyMessage() {

		WebServiceMessage message = this.messageFactory.createWebServiceMessage();

		assertThat(message).isNotNull();
	}

	protected abstract WebServiceMessageFactory createMessageFactory() throws Exception;

}
