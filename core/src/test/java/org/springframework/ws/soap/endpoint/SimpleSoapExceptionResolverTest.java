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

package org.springframework.ws.soap.endpoint;

import java.util.Locale;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.context.SoapMessageContext;
import org.springframework.ws.soap.soap11.Soap11Body;

public class SimpleSoapExceptionResolverTest extends TestCase {

    private SimpleSoapExceptionResolver exceptionResolver;

    private MockControl contextControl;

    private SoapMessageContext contextMock;

    private MockControl messageControl;

    private SoapMessage messageMock;

    private MockControl bodyControl;

    private Soap11Body bodyMock;

    protected void setUp() throws Exception {
        exceptionResolver = new SimpleSoapExceptionResolver();
        contextControl = MockControl.createControl(SoapMessageContext.class);
        contextMock = (SoapMessageContext) contextControl.getMock();
        messageControl = MockControl.createControl(SoapMessage.class);
        messageMock = (SoapMessage) messageControl.getMock();
        bodyControl = MockControl.createControl(Soap11Body.class);
        bodyControl.setDefaultMatcher(MockControl.ARRAY_MATCHER);
        bodyMock = (Soap11Body) bodyControl.getMock();
    }

    public void testResolveExceptionInternal() throws Exception {
        Exception exception = new Exception("message");
        contextControl.expectAndReturn(contextMock.getSoapResponse(), messageMock);
        messageControl.expectAndReturn(messageMock.getSoapBody(), bodyMock);
        bodyControl.expectAndReturn(bodyMock.addServerOrReceiverFault(exception.getMessage(), Locale.ENGLISH), null);
        contextControl.replay();
        messageControl.replay();
        bodyControl.replay();
        boolean result = exceptionResolver.resolveExceptionInternal(contextMock, null, exception);
        assertTrue("Invalid result", result);
        contextControl.verify();
        messageControl.verify();
        bodyControl.verify();
    }
}