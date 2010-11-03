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
import java.util.Collections;
import java.util.Map;

import org.springframework.ws.WebServiceMessage;
import org.springframework.xml.transform.StringSource;

import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNotNull;

public class DefaultXPathExpectationsTest {

    @Test
    public void existsMatch() throws IOException, AssertionError {
        RequestMatcher requestMatcher = RequestMatchers.xpath("//b").exists();
        assertNotNull(requestMatcher);

        WebServiceMessage message = createMock(WebServiceMessage.class);
        expect(message.getPayloadSource()).andReturn(new StringSource("<a><b/></a>"));

        replay(message);

        requestMatcher.match(null, message);

        verify(message);
    }

    @Test(expected = AssertionError.class)
    public void existsNonMatch() throws IOException, AssertionError {
        RequestMatcher requestMatcher = RequestMatchers.xpath("//c").exists();
        assertNotNull(requestMatcher);

        WebServiceMessage message = createMock(WebServiceMessage.class);
        expect(message.getPayloadSource()).andReturn(new StringSource("<a><b/></a>"));

        replay(message);

        requestMatcher.match(null, message);
    }

    @Test
    public void notExistsMatch() throws IOException, AssertionError {
        RequestMatcher requestMatcher = RequestMatchers.xpath("//c").doesNotExist();
        assertNotNull(requestMatcher);

        WebServiceMessage message = createMock(WebServiceMessage.class);
        expect(message.getPayloadSource()).andReturn(new StringSource("<a><b/></a>"));

        replay(message);

        requestMatcher.match(null, message);

        verify(message);
    }

    @Test(expected = AssertionError.class)
    public void notExistsNonMatch() throws IOException, AssertionError {
        RequestMatcher requestMatcher = RequestMatchers.xpath("//a").doesNotExist();
        assertNotNull(requestMatcher);

        WebServiceMessage message = createMock(WebServiceMessage.class);
        expect(message.getPayloadSource()).andReturn(new StringSource("<a><b/></a>"));

        replay(message);

        requestMatcher.match(null, message);
    }

    @Test
    public void evaluatesToTrueMatch() throws IOException, AssertionError {
        RequestMatcher requestMatcher = RequestMatchers.xpath("//b=1").evaluatesTo(true);
        assertNotNull(requestMatcher);

        WebServiceMessage message = createMock(WebServiceMessage.class);
        expect(message.getPayloadSource()).andReturn(new StringSource("<a><b>1</b></a>"));

        replay(message);

        requestMatcher.match(null, message);

        verify(message);
    }

    @Test(expected = AssertionError.class)
    public void evaluatesToTrueNonMatch() throws IOException, AssertionError {
        RequestMatcher requestMatcher = RequestMatchers.xpath("//b=2").evaluatesTo(true);
        assertNotNull(requestMatcher);

        WebServiceMessage message = createMock(WebServiceMessage.class);
        expect(message.getPayloadSource()).andReturn(new StringSource("<a><b>1</b></a>"));

        replay(message);

        requestMatcher.match(null, message);
    }

    @Test
    public void evaluatesToFalseMatch() throws IOException, AssertionError {
        RequestMatcher requestMatcher = RequestMatchers.xpath("//b!=1").evaluatesTo(false);
        assertNotNull(requestMatcher);

        WebServiceMessage message = createMock(WebServiceMessage.class);
        expect(message.getPayloadSource()).andReturn(new StringSource("<a><b>1</b></a>"));

        replay(message);

        requestMatcher.match(null, message);

        verify(message);
    }

    @Test(expected = AssertionError.class)
    public void evaluatesToFalseNonMatch() throws IOException, AssertionError {
        RequestMatcher requestMatcher = RequestMatchers.xpath("//b!=2").evaluatesTo(false);
        assertNotNull(requestMatcher);

        WebServiceMessage message = createMock(WebServiceMessage.class);
        expect(message.getPayloadSource()).andReturn(new StringSource("<a><b>1</b></a>"));

        replay(message);

        requestMatcher.match(null, message);
    }

    @Test
    public void existsWithNamespacesMatch() throws IOException, AssertionError {
        Map<String, String> ns = Collections.singletonMap("x", "http://example.org");
        RequestMatcher requestMatcher = RequestMatchers.xpath("//x:b", ns).exists();
        assertNotNull(requestMatcher);

        WebServiceMessage message = createMock(WebServiceMessage.class);
        expect(message.getPayloadSource())
                .andReturn(new StringSource("<a:a xmlns:a=\"http://example.org\"><a:b/></a:a>"));

        replay(message);

        requestMatcher.match(null, message);

        verify(message);
    }

    @Test(expected = AssertionError.class)
    public void existsWithNamespacesNonMatch() throws IOException, AssertionError {
        Map<String, String> ns = Collections.singletonMap("x", "http://example.org");
        RequestMatcher requestMatcher = RequestMatchers.xpath("//b", ns).exists();
        assertNotNull(requestMatcher);

        WebServiceMessage message = createMock(WebServiceMessage.class);
        expect(message.getPayloadSource())
                .andReturn(new StringSource("<a:a xmlns:a=\"http://example.org\"><a:b/></a:a>"));

        replay(message);

        requestMatcher.match(null, message);
    }

    @Test
    public void evaluatesToIntegerMatch() throws IOException, AssertionError {
        RequestMatcher requestMatcher = RequestMatchers.xpath("//b").evaluatesTo(1);
        assertNotNull(requestMatcher);

        WebServiceMessage message = createMock(WebServiceMessage.class);
        expect(message.getPayloadSource()).andReturn(new StringSource("<a><b>1</b></a>"));

        replay(message);

        requestMatcher.match(null, message);

        verify(message);
    }

    @Test(expected = AssertionError.class)
    public void evaluatesToIntegerNonMatch() throws IOException, AssertionError {
        RequestMatcher requestMatcher = RequestMatchers.xpath("//b").evaluatesTo(2);
        assertNotNull(requestMatcher);

        WebServiceMessage message = createMock(WebServiceMessage.class);
        expect(message.getPayloadSource()).andReturn(new StringSource("<a><b>1</b></a>"));

        replay(message);

        requestMatcher.match(null, message);
    }

}
