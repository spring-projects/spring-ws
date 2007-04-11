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

package org.springframework.ws.soap.server.endpoint.mapping;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.server.endpoint.annotation.SoapAction;

public class SoapActionAnnotationMethodEndpointMappingTest extends TestCase {

    private SoapActionAnnotationMethodEndpointMapping mapping;

    private StaticApplicationContext applicationContext;

    protected void setUp() throws Exception {
        applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("mapping", SoapActionAnnotationMethodEndpointMapping.class);
        applicationContext.registerSingleton("endpoint", MyEndpoint.class);
        applicationContext.refresh();
        mapping = (SoapActionAnnotationMethodEndpointMapping) applicationContext.getBean("mapping");
    }

    public void testRegistration() throws Exception {
        MockControl messageControl = MockControl.createControl(SoapMessage.class);
        SoapMessage requestMock = (SoapMessage) messageControl.getMock();
        messageControl.expectAndReturn(requestMock.getSoapAction(), "http://springframework.org/spring-ws/SoapAction");
        messageControl.replay();
        MockControl factoryControl = MockControl.createControl(WebServiceMessageFactory.class);
        WebServiceMessageFactory factoryMock = (WebServiceMessageFactory) factoryControl.getMock();
        factoryControl.replay();
        MessageContext context = new DefaultMessageContext(requestMock, factoryMock);

        EndpointInvocationChain chain = mapping.getEndpoint(context);
        assertNotNull("MethodEndpoint not registered", chain);
        MethodEndpoint expected = new MethodEndpoint(applicationContext.getBean("endpoint"), "doIt", new Class[0]);
        assertEquals("Invalid endpoint registered", expected, chain.getEndpoint());
        messageControl.verify();
        factoryControl.verify();
    }

    @Endpoint
    private static class MyEndpoint {

        @SoapAction("http://springframework.org/spring-ws/SoapAction")
        public void doIt() {

        }

    }
}