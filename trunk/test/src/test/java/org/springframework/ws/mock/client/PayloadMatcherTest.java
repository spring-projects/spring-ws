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

package org.springframework.ws.mock.client;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.WebServiceMessage;
import org.springframework.xml.transform.StringSource;

import org.junit.Test;

import static org.easymock.EasyMock.*;

public class PayloadMatcherTest {

    @Test
    public void stringPayloadMatch() throws Exception {
        String xml = "<element xmlns='http://example.com'/>";
        WebServiceMessage message = createMock(WebServiceMessage.class);
        expect(message.getPayloadSource()).andReturn(new StringSource(xml));
        replay(message);

        PayloadMatcher matcher = PayloadMatcher.createStringPayloadMatcher(xml);
        matcher.match(message);

        verify(message);
    }

    @Test(expected = AssertionError.class)
    public void nonMatch() throws Exception {
        String actual = "<element1 xmlns='http://example.com'/>";
        WebServiceMessage message = createMock(WebServiceMessage.class);
        expect(message.getPayloadSource()).andReturn(new StringSource(actual));
        replay(message);

        String expected = "<element2 xmlns='http://example.com'/>";
        PayloadMatcher matcher = PayloadMatcher.createStringPayloadMatcher(expected);
        matcher.match(message);
    }

    @Test
    public void resourcePayload() throws Exception {
        String xml = "<element xmlns='http://example.com'/>";
        WebServiceMessage message = createMock(WebServiceMessage.class);
        expect(message.getPayloadSource()).andReturn(new StringSource(xml));
        replay(message);

        Resource resource = new ByteArrayResource(xml.getBytes());
        PayloadMatcher matcher = PayloadMatcher.createResourcePayloadMatcher(resource);
        matcher.match(message);

        verify(message);
    }


    @Test
    public void sourcePayloadMatcher() throws Exception {
        String xml = "<element xmlns='http://example.com'/>";
        WebServiceMessage message = createMock(WebServiceMessage.class);
        expect(message.getPayloadSource()).andReturn(new StringSource(xml));
        replay(message);

        Resource resource = new ByteArrayResource(xml.getBytes());
        PayloadMatcher matcher = PayloadMatcher.createSourcePayloadMatcher(new StringSource(xml));
        matcher.match(message);

        verify(message);
    }
}
