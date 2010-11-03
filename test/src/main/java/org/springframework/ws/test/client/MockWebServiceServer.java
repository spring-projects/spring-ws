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

import org.springframework.util.Assert;
import org.springframework.ws.client.core.WebServiceTemplate;

/**
 * @author Arjen Poutsma
 */
public class MockWebServiceServer {

    private final MockWebServiceMessageSender mockMessageSender;

    private MockWebServiceServer(MockWebServiceMessageSender mockMessageSender) {
        Assert.notNull(mockMessageSender, "'mockMessageSender' must not be null");
        this.mockMessageSender = mockMessageSender;
    }

    public static MockWebServiceServer createServer(WebServiceTemplate webServiceTemplate) {
        Assert.notNull(webServiceTemplate, "'webServiceTemplate' must not be null");

        MockWebServiceMessageSender mockMessageSender = new MockWebServiceMessageSender();
        webServiceTemplate.setMessageSender(mockMessageSender);

        return new MockWebServiceServer(mockMessageSender);
    }

    /**
     * Records an expectation specified by the given {@link RequestMatcher}. Returns a {@link ResponseActions} object
     * that allows for setting up the response, or more expectations.
     *
     * @param requestMatcher the request matcher expected
     * @return the response actions
     */
    public ResponseActions expect(RequestMatcher requestMatcher) {
        MockSenderConnection connection = mockMessageSender.expectNewConnection();
        connection.addRequestMatcher(requestMatcher);
        return connection;
    }

    /**
     * Verifies that all connections were used.
     *
     * @throws AssertionError in case of unused connections.
     */
    public void verify() {
        mockMessageSender.verifyConnections();
    }
    



}
