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
import static org.easymock.EasyMock.*;
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
    	SoapMessage requestMock = createMock(SoapMessage.class);
    	expect(requestMock.getSoapAction()).andReturn("http://springframework.org/spring-ws/SoapAction");
    	WebServiceMessageFactory factoryMock = createMock(WebServiceMessageFactory.class);
    	replay(requestMock, factoryMock);

    	MessageContext context = new DefaultMessageContext(requestMock, factoryMock);
        EndpointInvocationChain chain = mapping.getEndpoint(context);
        assertNotNull("MethodEndpoint not registered", chain);
        MethodEndpoint expected = new MethodEndpoint(applicationContext.getBean("endpoint"), "doIt", new Class[0]);
        assertEquals("Invalid endpoint registered", expected, chain.getEndpoint());
        
        verify(requestMock,factoryMock);
    }

    @Endpoint
    private static class MyEndpoint {

        @SoapAction("http://springframework.org/spring-ws/SoapAction")
        public void doIt() {

        }

    }
}