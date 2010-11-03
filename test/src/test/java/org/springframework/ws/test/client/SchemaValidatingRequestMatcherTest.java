/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.test.client;

import java.io.IOException;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.WebServiceMessage;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;

public class SchemaValidatingRequestMatcherTest {

    private Resource schema2;

    private Resource schema1;

    private WebServiceMessage message;

    @Before
    public void setUp() {
        message = createMock(WebServiceMessage.class);
        schema1 = new ClassPathResource("schemaValidatingRequestMatcherTest.xsd", SchemaValidatingRequestMatcherTest.class);
        schema2 = new ByteArrayResource("".getBytes());
    }

    @Test
    public void singleSchemaMatch() throws IOException, AssertionError {
        expect(message.getPayloadSource()).andReturn(new StringSource(
                "<test xmlns=\"http://www.example.org/schema\"><number>0</number><text>text</text></test>"));

        RequestMatcher requestMatcher = RequestMatchers.validPayload(schema1);

        replay(message);

        requestMatcher.match(null, message);

        verify(message);
    }

    @Test(expected = AssertionError.class)
    public void singleSchemaNonMatch() throws IOException, AssertionError {
        expect(message.getPayloadSource()).andReturn(new StringSource(
                "<test xmlns=\"http://www.example.org/schema\"><number>a</number><text>text</text></test>"));

        RequestMatcher requestMatcher = RequestMatchers.validPayload(schema1);

        replay(message);

        requestMatcher.match(null, message);

        verify(message);
    }

    @Test
    public void multipleSchemaMatch() throws IOException, AssertionError {
        expect(message.getPayloadSource()).andReturn(new StringSource(
                "<test xmlns=\"http://www.example.org/schema\"><number>0</number><text>text</text></test>"));

        RequestMatcher requestMatcher = RequestMatchers.validPayload(schema1, schema2);

        replay(message);

        requestMatcher.match(null, message);

        verify(message);
    }

    @Test(expected = AssertionError.class)
    public void multipleSchemaNotOk() throws IOException, AssertionError {
        expect(message.getPayloadSource()).andReturn(new StringSource(
                "<test xmlns=\"http://www.example.org/schema\"><number>a</number><text>text</text></test>"));

        RequestMatcher requestMatcher = RequestMatchers.validPayload(schema1, schema2);

        replay(message);

        requestMatcher.match(null, message);

        verify(message);
    }

    @Test(expected = AssertionError.class)
    public void multipleSchemaDifferentOrderNotOk() throws IOException, AssertionError {
        expect(message.getPayloadSource()).andReturn(new StringSource(
                "<test xmlns=\"http://www.example.org/schema\"><number>a</number><text>text</text></test>"));

        RequestMatcher requestMatcher = RequestMatchers.validPayload(schema2, schema1);

        replay(message);

        requestMatcher.match(null, message);

        verify(message);
    }

    @Test(expected = AssertionError.class)
    public void xmlValidatorNotOk() throws IOException, AssertionError {
        expect(message.getPayloadSource()).andReturn(new StringSource(
                "<test xmlns=\"http://www.example.org/schema\"><number>a</number><text>text</text></test>"));

        XmlValidator validator = XmlValidatorFactory.createValidator(schema1, XmlValidatorFactory.SCHEMA_W3C_XML);
        RequestMatcher requestMatcher = new SchemaValidatingRequestMatcher(validator);

        replay(message);

        requestMatcher.match(null, message);

        verify(message);
    }
}
