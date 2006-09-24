/*
 * Copyright 2005 the original author or authors.
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

package org.springframework.ws.mock.soap;

import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.context.AbstractSoapMessageContext;

/**
 * @author Arjen Poutsma
 */
public class MockSoapMessageContext extends AbstractSoapMessageContext {

    private MockSoapMessage request;

    private MockSoapMessage response;

    public MockSoapMessageContext() {
        this.request = new MockSoapMessage();
    }

    public MockSoapMessageContext(MockSoapMessage request) {
        this.request = request;
    }

    public SoapMessage getSoapRequest() {
        return request;
    }

    public SoapMessage createSoapResponse() {
        if (response != null) {
            throw new IllegalStateException("Response already created");
        }
        else {
            response = new MockSoapMessage();
            return response;
        }
    }

    public SoapMessage getSoapResponse() {
        return response;
    }

}
