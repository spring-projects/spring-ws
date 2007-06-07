/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.ws.client.core;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceFaultException;

public class SimpleFaultMessageResolverTest extends TestCase {

    private SimpleFaultMessageResolver resolver;

    protected void setUp() throws Exception {
        resolver = new SimpleFaultMessageResolver();
    }

    public void testResolveFault() throws Exception {
        MockControl messageControl = MockControl.createControl(WebServiceMessage.class);
        WebServiceMessage messageMock = (WebServiceMessage) messageControl.getMock();
        String message = "message";
        messageControl.expectAndReturn(messageMock.getFaultReason(), message);
        messageControl.replay();
        try {
            resolver.resolveFault(messageMock);
            fail("WebServiceFaultExcpetion expected");
        }
        catch (WebServiceFaultException ex) {
            // expected
            assertEquals("Invalid exception message", message, ex.getMessage());
        }
        messageControl.verify();
    }
}