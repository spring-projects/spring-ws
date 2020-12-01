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

package org.springframework.ws.wsdl.wsdl11;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

public class SimpleWsdl11DefinitionTest {

	private SimpleWsdl11Definition definition;

	@BeforeEach
	public void setUp() throws Exception {

		definition = new SimpleWsdl11Definition();
		definition.setWsdl(new ClassPathResource("complete.wsdl", getClass()));
		definition.afterPropertiesSet();
	}

	@Test
	public void testGetSource() {
		assertThat(definition.getSource()).isNotNull();
	}
}
