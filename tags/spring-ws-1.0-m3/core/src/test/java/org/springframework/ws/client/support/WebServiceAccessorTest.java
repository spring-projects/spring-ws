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

package org.springframework.ws.client.support;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.WebServiceMessageSender;

public class WebServiceAccessorTest extends TestCase {

    private WebServiceAccessor accessor;

    protected void setUp() throws Exception {
        accessor = new MyWebServiceAccessor();
    }

    public void testCreateMessageContext() throws Exception {
        MockControl senderControl = MockControl.createControl(WebServiceMessageSender.class);
        WebServiceMessageSender senderMock = (WebServiceMessageSender) senderControl.getMock();
        accessor.setMessageSender(senderMock);
        MockControl factoryControl = MockControl.createControl(WebServiceMessageFactory.class);
        WebServiceMessageFactory factoryMock = (WebServiceMessageFactory) factoryControl.getMock();
        accessor.setMessageFactory(factoryMock);
        MockControl messageControl = MockControl.createControl(WebServiceMessage.class);
        WebServiceMessage messageMock = (WebServiceMessage) messageControl.getMock();
        accessor.afterPropertiesSet();

        factoryControl.expectAndReturn(factoryMock.createWebServiceMessage(), messageMock);

        factoryControl.replay();
        messageControl.replay();
        MessageContext messageContext = accessor.createMessageContext();
        assertNotNull("No MesageContext created", messageContext);
        factoryControl.verify();
        messageControl.verify();
    }

    private static class MyWebServiceAccessor extends WebServiceAccessor {

    }
}