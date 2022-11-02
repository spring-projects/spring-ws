/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ws;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractWebServiceMessageFactoryTestCase {

	protected WebServiceMessageFactory messageFactory;

	@BeforeEach
	public final void setUp() throws Exception {
		messageFactory = createMessageFactory();
	}

	@Test
	public void testCreateEmptyMessage() throws Exception {

		WebServiceMessage message = messageFactory.createWebServiceMessage();

		assertThat(message).isNotNull();
	}

	protected abstract WebServiceMessageFactory createMessageFactory() throws Exception;
}
