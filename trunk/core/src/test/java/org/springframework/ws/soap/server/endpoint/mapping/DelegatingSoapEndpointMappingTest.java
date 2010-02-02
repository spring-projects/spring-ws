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

package org.springframework.ws.soap.server.endpoint.mapping;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.soap.server.SoapEndpointInvocationChain;

public class DelegatingSoapEndpointMappingTest extends TestCase {

    private DelegatingSoapEndpointMapping endpointMapping;

    private MockControl control;

    private EndpointMapping mock;

    @Override
    protected void setUp() throws Exception {
        endpointMapping = new DelegatingSoapEndpointMapping();
        control = MockControl.createControl(EndpointMapping.class);
        mock = (EndpointMapping) control.getMock();
        endpointMapping.setDelegate(mock);
    }

    public void testGetEndpointMapping() throws Exception {
        String role = "http://www.springframework.org/spring-ws/role";
        endpointMapping.setActorOrRole(role);
        MessageContext context = new DefaultMessageContext(new MockWebServiceMessageFactory());
        EndpointInvocationChain delegateChain = new EndpointInvocationChain(new Object());
        control.expectAndReturn(mock.getEndpoint(context), delegateChain);
        control.replay();
        SoapEndpointInvocationChain resultChain = (SoapEndpointInvocationChain) endpointMapping.getEndpoint(context);
        assertNotNull("No chain returned", resultChain);
        assertEquals("Invalid ampount of roles returned", 1, resultChain.getActorsOrRoles().length);
        assertEquals("Invalid role returned", role, resultChain.getActorsOrRoles()[0]);
        control.verify();
    }
}