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

package org.springframework.ws.test.support.matcher;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.WebServiceMessage;
import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

class SchemaValidatingMatcherTests {

	private Resource schema;

	private Resource schema1;

	private Resource schema2;

	private WebServiceMessage message;

	@BeforeEach
	void setUp() {

		this.message = createMock(WebServiceMessage.class);
		this.schema = new ClassPathResource("schemaValidatingMatcherTest.xsd", SchemaValidatingMatcherTests.class);
		this.schema1 = new ClassPathResource("schemaValidatingMatcherTest-1.xsd", SchemaValidatingMatcherTests.class);
		this.schema2 = new ClassPathResource("schemaValidatingMatcherTest-2.xsd", SchemaValidatingMatcherTests.class);
	}

	@Test
	void singleSchemaMatch() throws IOException, AssertionError {

		expect(this.message.getPayloadSource()).andReturn(new StringSource(
				"<test xmlns=\"http://www.example.org/schema\"><number>0</number><text>text</text></test>"));

		SchemaValidatingMatcher matcher = new SchemaValidatingMatcher(this.schema);

		replay(this.message);

		matcher.match(this.message);

		verify(this.message);
	}

	@Test
	void singleSchemaNonMatch() throws AssertionError {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			expect(this.message.getPayloadSource())
				.andReturn(new StringSource(
						"<test xmlns=\"http://www.example.org/schema\"><number>a</number><text>text</text></test>"))
				.times(2);

			SchemaValidatingMatcher matcher = new SchemaValidatingMatcher(this.schema);

			replay(this.message);

			matcher.match(this.message);

			verify(this.message);
		});
	}

	@Test
	void multipleSchemaMatch() throws IOException, AssertionError {

		expect(this.message.getPayloadSource()).andReturn(new StringSource(
				"<test xmlns=\"http://www.example.org/schema\"><number>0</number><text>text</text></test>"));

		SchemaValidatingMatcher matcher = new SchemaValidatingMatcher(this.schema1, this.schema2);

		replay(this.message);

		matcher.match(this.message);

		verify(this.message);
	}

	@Test
	void multipleSchemaNotOk() throws AssertionError {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			expect(this.message.getPayloadSource())
				.andReturn(new StringSource(
						"<test xmlns=\"http://www.example.org/schema\"><number>a</number><text>text</text></test>"))
				.times(2);

			SchemaValidatingMatcher matcher = new SchemaValidatingMatcher(this.schema1, this.schema2);

			replay(this.message);

			matcher.match(this.message);

			verify(this.message);
		});
	}

	@Test
	void multipleSchemaDifferentOrderNotOk() throws AssertionError {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			expect(this.message.getPayloadSource())
				.andReturn(new StringSource(
						"<test xmlns=\"http://www.example.org/schema\"><number>a</number><text>text</text></test>"))
				.times(2);

			SchemaValidatingMatcher matcher = new SchemaValidatingMatcher(this.schema1, this.schema2);

			replay(this.message);

			matcher.match(this.message);

			verify(this.message);
		});
	}

	@Test
	void xmlValidatorNotOk() throws AssertionError {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			expect(this.message.getPayloadSource())
				.andReturn(new StringSource(
						"<test xmlns=\"http://www.example.org/schema\"><number>a</number><text>text</text></test>"))
				.times(2);

			SchemaValidatingMatcher matcher = new SchemaValidatingMatcher(this.schema);

			replay(this.message);

			matcher.match(this.message);

			verify(this.message);
		});
	}

}
