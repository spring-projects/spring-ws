/*
 * Copyright 2005-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.test.support.matcher;

import static org.easymock.EasyMock.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.WebServiceMessage;
import org.springframework.xml.transform.StringSource;

public class SchemaValidatingMatcherTest {

	private Resource schema;

	private Resource schema1;

	private Resource schema2;

	private WebServiceMessage message;

	@Before
	public void setUp() {
		message = createMock(WebServiceMessage.class);
		schema = new ClassPathResource("schemaValidatingMatcherTest.xsd", SchemaValidatingMatcherTest.class);
		schema1 = new ClassPathResource("schemaValidatingMatcherTest-1.xsd", SchemaValidatingMatcherTest.class);
		schema2 = new ClassPathResource("schemaValidatingMatcherTest-2.xsd", SchemaValidatingMatcherTest.class);
	}

	@Test
	public void singleSchemaMatch() throws IOException, AssertionError {
		expect(message.getPayloadSource()).andReturn(
				new StringSource("<test xmlns=\"http://www.example.org/schema\"><number>0</number><text>text</text></test>"));

		SchemaValidatingMatcher matcher = new SchemaValidatingMatcher(schema);

		replay(message);

		matcher.match(message);

		verify(message);
	}

	@Test(expected = AssertionError.class)
	public void singleSchemaNonMatch() throws IOException, AssertionError {
		expect(message.getPayloadSource()).andReturn(
				new StringSource("<test xmlns=\"http://www.example.org/schema\"><number>a</number><text>text</text></test>"))
				.times(2);

		SchemaValidatingMatcher matcher = new SchemaValidatingMatcher(schema);

		replay(message);

		matcher.match(message);

		verify(message);
	}

	@Test
	public void multipleSchemaMatch() throws IOException, AssertionError {
		expect(message.getPayloadSource()).andReturn(
				new StringSource("<test xmlns=\"http://www.example.org/schema\"><number>0</number><text>text</text></test>"));

		SchemaValidatingMatcher matcher = new SchemaValidatingMatcher(schema1, schema2);

		replay(message);

		matcher.match(message);

		verify(message);
	}

	@Test(expected = AssertionError.class)
	public void multipleSchemaNotOk() throws IOException, AssertionError {
		expect(message.getPayloadSource()).andReturn(
				new StringSource("<test xmlns=\"http://www.example.org/schema\"><number>a</number><text>text</text></test>"))
				.times(2);

		SchemaValidatingMatcher matcher = new SchemaValidatingMatcher(schema1, schema2);

		replay(message);

		matcher.match(message);

		verify(message);
	}

	@Test(expected = AssertionError.class)
	public void multipleSchemaDifferentOrderNotOk() throws IOException, AssertionError {
		expect(message.getPayloadSource()).andReturn(
				new StringSource("<test xmlns=\"http://www.example.org/schema\"><number>a</number><text>text</text></test>"))
				.times(2);

		SchemaValidatingMatcher matcher = new SchemaValidatingMatcher(schema1, schema2);

		replay(message);

		matcher.match(message);

		verify(message);
	}

	@Test(expected = AssertionError.class)
	public void xmlValidatorNotOk() throws IOException, AssertionError {
		expect(message.getPayloadSource()).andReturn(
				new StringSource("<test xmlns=\"http://www.example.org/schema\"><number>a</number><text>text</text></test>"))
				.times(2);

		SchemaValidatingMatcher matcher = new SchemaValidatingMatcher(schema);

		replay(message);

		matcher.match(message);

		verify(message);
	}
}
