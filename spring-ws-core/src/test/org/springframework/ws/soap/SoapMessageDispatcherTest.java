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

package org.springframework.ws.soap;

import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.ws.mock.soap.MockSoapMessage;
import org.springframework.ws.mock.soap.MockSoapMessageContext;
import org.w3c.dom.Element;

public class SoapMessageDispatcherTest extends TestCase {

    private SoapMessageDispatcher dispatcher;

    protected void setUp() throws Exception {
        dispatcher = new SoapMessageDispatcher();
    }

    public void testProcessMustUnderstandHeadersUnderstoor() throws Exception {
        MockSoapMessage request = new MockSoapMessage();
        QName qName = new QName("namespace", "header");
        Element header = request.addHeaderElement(qName, true, null);
        MockControl interceptorControl = MockControl.createControl(SoapEndpointInterceptor.class);
        SoapEndpointInterceptor interceptorMock = (SoapEndpointInterceptor) interceptorControl.getMock();

        SoapEndpointInvocationChain chain =
                new SoapEndpointInvocationChain(new Object(), new SoapEndpointInterceptor[]{interceptorMock});

        interceptorControl.expectAndReturn(interceptorMock.understands(header), true);
        interceptorControl.replay();

        MockSoapMessageContext context = new MockSoapMessageContext(request);
        boolean result = dispatcher.handleRequest(chain, context);
        assertTrue("Invalid result", result);
        assertNull("Response not expected", context.getResponse());
        interceptorControl.verify();
    }

    public void testProcessMustUnderstandHeadersNotUnderstood() throws Exception {
        MockSoapMessage request = new MockSoapMessage();
        QName qName = new QName("namespace", "header");
        Element header = request.addHeaderElement(qName, true, null);
        MockControl interceptorControl = MockControl.createControl(SoapEndpointInterceptor.class);
        SoapEndpointInterceptor interceptorMock = (SoapEndpointInterceptor) interceptorControl.getMock();

        SoapEndpointInvocationChain chain =
                new SoapEndpointInvocationChain(new Object(), new SoapEndpointInterceptor[]{interceptorMock});

        interceptorControl.expectAndReturn(interceptorMock.understands(header), false);
        interceptorControl.replay();

        MockSoapMessageContext context = new MockSoapMessageContext(request);
        boolean result = dispatcher.handleRequest(chain, context);
        assertFalse("Invalid result", result);
        assertNotNull("Response expected", context.getResponse());
        assertNotNull("Response message has no fault", ((SoapMessage) context.getResponse()).getFault());
        interceptorControl.verify();
    }

    public void testProcessMustUnderstandHeadersInRole() throws Exception {
        MockSoapMessage request = new MockSoapMessage();
        QName qName = new QName("namespace", "header");
        Element header = request.addHeaderElement(qName, true, "actor");
        MockControl interceptorControl = MockControl.createControl(SoapEndpointInterceptor.class);
        SoapEndpointInterceptor interceptorMock = (SoapEndpointInterceptor) interceptorControl.getMock();

        SoapEndpointInvocationChain chain =
                new SoapEndpointInvocationChain(new Object(),
                        new SoapEndpointInterceptor[]{interceptorMock},
                        new String[]{"actor"});

        interceptorControl.expectAndReturn(interceptorMock.understands(header), true);
        interceptorControl.replay();

        MockSoapMessageContext context = new MockSoapMessageContext(request);
        boolean result = dispatcher.handleRequest(chain, context);
        assertTrue("Invalid result", result);
        assertNull("Response not expected", context.getResponse());
        interceptorControl.verify();
    }

}