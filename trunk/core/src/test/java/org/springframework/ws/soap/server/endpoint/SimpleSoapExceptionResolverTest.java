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

package org.springframework.ws.soap.server.endpoint;

import java.util.Locale;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.soap11.Soap11Body;

public class SimpleSoapExceptionResolverTest extends TestCase {

    private SimpleSoapExceptionResolver exceptionResolver;

    private MessageContext messageContext;

    private MockControl messageControl;

    private SoapMessage messageMock;

    private MockControl bodyControl;

    private Soap11Body bodyMock;

    private MockControl factoryControl;

    private WebServiceMessageFactory factoryMock;

    @Override
    protected void setUp() throws Exception {
        exceptionResolver = new SimpleSoapExceptionResolver();
        factoryControl = MockControl.createControl(WebServiceMessageFactory.class);
        factoryMock = (WebServiceMessageFactory) factoryControl.getMock();
        messageContext = new DefaultMessageContext(new MockWebServiceMessage(), factoryMock);
        messageControl = MockControl.createControl(SoapMessage.class);
        messageMock = (SoapMessage) messageControl.getMock();
        bodyControl = MockControl.createControl(Soap11Body.class);
        bodyControl.setDefaultMatcher(MockControl.ARRAY_MATCHER);
        bodyMock = (Soap11Body) bodyControl.getMock();
    }

    public void testResolveExceptionInternal() throws Exception {
        Exception exception = new Exception("message");
        factoryControl.expectAndReturn(factoryMock.createWebServiceMessage(), messageMock);
        messageControl.expectAndReturn(messageMock.getSoapBody(), bodyMock);
        bodyControl.expectAndReturn(bodyMock.addServerOrReceiverFault(exception.getMessage(), Locale.ENGLISH), null);
        factoryControl.replay();
        messageControl.replay();
        bodyControl.replay();
        boolean result = exceptionResolver.resolveExceptionInternal(messageContext, null, exception);
        assertTrue("Invalid result", result);
        factoryControl.verify();
        messageControl.verify();
        bodyControl.verify();
    }
}