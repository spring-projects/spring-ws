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

import java.io.IOException;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.AbstractMessageContext;
import org.springframework.ws.transport.TransportContext;
import org.springframework.ws.transport.TransportResponse;

/**
 * Mock implementation of the <code>MessageContext</code> interface.
 *
 * @author Arjen Poutsma
 */
public class MockMessageContext extends AbstractMessageContext {

    private TransportResponse transportResponse;

    public MockMessageContext() {
        super(new MockWebServiceMessage(), new MockTransportRequest());
    }

    public MockMessageContext(MockWebServiceMessage request) {
        super(request, new MockTransportRequest());
    }

    public MockMessageContext(MockWebServiceMessage request, TransportContext transportContext) {
        super(request, transportContext.getTransportRequest());
        transportResponse = transportContext.getTransportResponse();
    }

    public MockMessageContext(String content) {
        super(new MockWebServiceMessage(content), new MockTransportRequest());
    }

    protected WebServiceMessage createResponseMessage() {
        return new MockWebServiceMessage();
    }

    public void sendResponse(TransportResponse transportResponse) throws IOException {
        getResponse().writeTo(this.transportResponse.getOutputStream());
    }
}
