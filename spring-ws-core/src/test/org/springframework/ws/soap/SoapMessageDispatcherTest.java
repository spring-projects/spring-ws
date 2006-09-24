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

import java.util.Collections;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.ws.soap.context.SoapMessageContext;

public class SoapMessageDispatcherTest extends TestCase {

    private SoapMessageDispatcher dispatcher;

    private MockControl messageControl;

    private SoapMessage requestMock;

    private SoapHeader headerMock;

    private MockControl headerControl;

    private MockControl interceptorControl;

    private SoapEndpointInterceptor interceptorMock;

    private MockControl contextControl;

    private SoapMessageContext contextMock;

    private MockControl headerElementControl;

    private SoapHeaderElement headerElementMock;

    private SoapMessage responseMock;

    private MockControl bodyControl;

    private SoapBody bodyMock;

    private MockControl faultControl;

    private SoapFault faultMock;

    private MockControl versionControl;

    private SoapVersion versionMock;

    protected void setUp() throws Exception {
        contextControl = MockControl.createControl(SoapMessageContext.class);
        contextMock = (SoapMessageContext) contextControl.getMock();
        messageControl = MockControl.createControl(SoapMessage.class);
        requestMock = (SoapMessage) messageControl.getMock();
        responseMock = (SoapMessage) messageControl.getMock();
        headerControl = MockControl.createControl(SoapHeader.class);
        headerMock = (SoapHeader) headerControl.getMock();
        headerElementControl = MockControl.createControl(SoapHeaderElement.class);
        headerElementMock = (SoapHeaderElement) headerElementControl.getMock();
        bodyControl = MockControl.createControl(SoapBody.class);
        bodyMock = (SoapBody) bodyControl.getMock();
        faultControl = MockControl.createControl(SoapFault.class);
        faultMock = (SoapFault) faultControl.getMock();
        interceptorControl = MockControl.createControl(SoapEndpointInterceptor.class);
        interceptorMock = (SoapEndpointInterceptor) interceptorControl.getMock();
        versionControl = MockControl.createControl(SoapVersion.class);
        versionMock = (SoapVersion) versionControl.getMock();
        dispatcher = new SoapMessageDispatcher();
    }

    public void testProcessMustUnderstandHeadersUnderstood() throws Exception {
        contextControl.expectAndReturn(contextMock.getSoapRequest(), requestMock);
        messageControl.expectAndReturn(requestMock.getSoapHeader(), headerMock);
        contextControl.expectAndReturn(contextMock.getSoapRequest(), requestMock);
        messageControl.expectAndReturn(requestMock.getVersion(), versionMock);
        String role = "next";
        versionControl.expectAndReturn(versionMock.getNextRoleUri(), role);
        contextControl.expectAndReturn(contextMock.getSoapRequest(), requestMock);
        messageControl.expectAndReturn(requestMock.getSoapHeader(), headerMock);
        headerControl.expectAndReturn(headerMock.examineMustUnderstandHeaderElements(role),
                Collections.singleton(headerElementMock).iterator());
        interceptorControl.expectAndReturn(interceptorMock.understands(headerElementMock), true);
        replayMockControls();

        SoapEndpointInvocationChain chain =
                new SoapEndpointInvocationChain(new Object(), new SoapEndpointInterceptor[]{interceptorMock});

        boolean result = dispatcher.handleRequest(chain, contextMock);
        assertTrue("Invalid result", result);
        verifyMockControls();
    }

    public void testProcessMustUnderstandHeadersNotUnderstood() throws Exception {
        contextControl.expectAndReturn(contextMock.getSoapRequest(), requestMock);
        messageControl.expectAndReturn(requestMock.getSoapHeader(), headerMock);
        contextControl.expectAndReturn(contextMock.getSoapRequest(), requestMock);
        messageControl.expectAndReturn(requestMock.getVersion(), versionMock);
        String role = "next";
        versionControl.expectAndReturn(versionMock.getNextRoleUri(), role);
        contextControl.expectAndReturn(contextMock.getSoapRequest(), requestMock);
        messageControl.expectAndReturn(requestMock.getSoapHeader(), headerMock);
        headerControl.expectAndReturn(headerMock.examineMustUnderstandHeaderElements(role),
                Collections.singleton(headerElementMock).iterator());
        interceptorControl.expectAndReturn(interceptorMock.understands(headerElementMock), false);
        QName headerElementName = new QName("header");
        headerElementControl.expectAndReturn(headerElementMock.getName(), headerElementName);
        contextControl.expectAndReturn(contextMock.createSoapResponse(), responseMock);
        messageControl.expectAndReturn(responseMock.getSoapBody(), bodyMock);
        bodyMock.addMustUnderstandFault(new QName[]{headerElementName});
        bodyControl.setMatcher(MockControl.ARRAY_MATCHER);
        bodyControl.setReturnValue(faultMock);
        faultMock.setFaultRole(role);

        replayMockControls();

        SoapEndpointInvocationChain chain =
                new SoapEndpointInvocationChain(new Object(), new SoapEndpointInterceptor[]{interceptorMock});

        boolean result = dispatcher.handleRequest(chain, contextMock);
        assertFalse("Invalid result", result);
        verifyMockControls();
    }

    public void testProcessMustUnderstandHeadersInRole() throws Exception {
        contextControl.expectAndReturn(contextMock.getSoapRequest(), requestMock);
        messageControl.expectAndReturn(requestMock.getSoapHeader(), headerMock);
        contextControl.expectAndReturn(contextMock.getSoapRequest(), requestMock);
        messageControl.expectAndReturn(requestMock.getVersion(), versionMock);
        String versionRole = "next";
        versionControl.expectAndReturn(versionMock.getNextRoleUri(), versionRole);
        contextControl.expectAndReturn(contextMock.getSoapRequest(), requestMock);
        messageControl.expectAndReturn(requestMock.getSoapHeader(), headerMock);
        String specifiedRole = "role";
        headerControl.expectAndReturn(headerMock.examineMustUnderstandHeaderElements(specifiedRole),
                Collections.singleton(headerElementMock).iterator());
        headerControl.expectAndReturn(headerMock.examineMustUnderstandHeaderElements(versionRole),
                Collections.singleton(headerElementMock).iterator());
        interceptorControl.expectAndReturn(interceptorMock.understands(headerElementMock), true);
        interceptorControl.expectAndReturn(interceptorMock.understands(headerElementMock), true);
        contextControl.expectAndReturn(contextMock.getSoapRequest(), requestMock);
        messageControl.expectAndReturn(requestMock.getSoapHeader(), headerMock);

        replayMockControls();

        SoapEndpointInvocationChain chain = new SoapEndpointInvocationChain(new Object(),
                new SoapEndpointInterceptor[]{interceptorMock}, new String[]{"role"});

        boolean result = dispatcher.handleRequest(chain, contextMock);
        assertTrue("Invalid result", result);
        verifyMockControls();
    }

    public void testProcessNoHeader() throws Exception {
        contextControl.expectAndReturn(contextMock.getSoapRequest(), requestMock);
        messageControl.expectAndReturn(requestMock.getSoapHeader(), null);

        replayMockControls();

        SoapEndpointInvocationChain chain = new SoapEndpointInvocationChain(new Object(),
                new SoapEndpointInterceptor[]{interceptorMock}, new String[]{"role"});

        boolean result = dispatcher.handleRequest(chain, contextMock);
        assertTrue("Invalid result", result);
        verifyMockControls();
    }

    private void replayMockControls() {
        contextControl.replay();
        messageControl.replay();
        headerControl.replay();
        headerElementControl.replay();
        bodyControl.replay();
        faultControl.replay();
        interceptorControl.replay();
        versionControl.replay();
    }

    private void verifyMockControls() {
        contextControl.verify();
        messageControl.verify();
        headerControl.verify();
        headerElementControl.verify();
        bodyControl.verify();
        faultControl.verify();
        interceptorControl.verify();
        versionControl.verify();
    }

    public void testGetRoles() throws Exception {
        String[] roles = dispatcher.getRoles(null, SoapVersion.SOAP_11);
        assertEquals("Invalid amount of roles", 1, roles.length);
        assertEquals("Invalid role", SoapVersion.SOAP_11.getNextRoleUri(), roles[0]);
        roles = dispatcher.getRoles(new String[]{"role"}, SoapVersion.SOAP_11);
        assertEquals("Invalid amount of roles", 2, roles.length);
        assertEquals("Invalid role", "role", roles[0]);
        assertEquals("Invalid role", SoapVersion.SOAP_11.getNextRoleUri(), roles[1]);
    }
}