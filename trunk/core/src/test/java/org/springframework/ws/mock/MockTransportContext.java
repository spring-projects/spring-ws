/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.mock;

import org.springframework.ws.transport.TransportContext;
import org.springframework.ws.transport.TransportRequest;
import org.springframework.ws.transport.TransportResponse;

/**
 * Mock implementation of the <code>TransportContext</code> interface.
 *
 * @author Arjen Poutsma
 */
public class MockTransportContext implements TransportContext {

    private MockTransportRequest request;

    private MockTransportResponse response;

    public MockTransportContext() {
        request = new MockTransportRequest();
        response = new MockTransportResponse();
    }

    public MockTransportContext(MockTransportRequest request) {
        this.request = request;
    }

    public MockTransportContext(MockTransportRequest request, MockTransportResponse response) {
        this.request = request;
        this.response = response;
    }

    public TransportRequest getTransportRequest() {
        return request;
    }

    public TransportResponse getTransportResponse() {
        return response;
    }
}
