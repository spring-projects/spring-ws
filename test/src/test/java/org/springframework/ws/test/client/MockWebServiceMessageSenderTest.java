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
import java.net.URI;

import org.junit.Before;
import org.junit.Test;

public class MockWebServiceMessageSenderTest {

    private MockWebServiceMessageSender sender;

    @Before
    public void setUp() throws Exception {
        sender = new MockWebServiceMessageSender();
    }

    @Test(expected = AssertionError.class)
    public void noMoreExpectedConnections() throws IOException {
        sender.createConnection(URI.create("http://localhost"));
    }

    @Test(expected = AssertionError.class)
    public void verify() throws IOException {
        sender.expectNewConnection();
        sender.verifyConnections();
    }

    @Test(expected = AssertionError.class)
    public void verifyMoteThanOne() throws IOException {
        sender.expectNewConnection();
        sender.expectNewConnection();
        sender.createConnection(URI.create("http://localhost"));
        sender.verifyConnections();
    }
}
