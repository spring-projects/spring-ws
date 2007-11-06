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

package org.springframework.ws.context;

import java.util.Arrays;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

public class DefaultMessageContextTest extends TestCase {

    private DefaultMessageContext context;

    private MockControl factoryControl;

    private WebServiceMessageFactory factoryMock;

    private WebServiceMessage request;

    protected void setUp() throws Exception {
        factoryControl = MockControl.createControl(WebServiceMessageFactory.class);
        factoryMock = (WebServiceMessageFactory) factoryControl.getMock();
        request = new MockWebServiceMessage();
        context = new DefaultMessageContext(request, factoryMock);
    }

    public void testRequest() throws Exception {
        assertEquals("Invalid request returned", request, context.getRequest());
    }

    public void testResponse() throws Exception {
        WebServiceMessage response = new MockWebServiceMessage();
        factoryControl.expectAndReturn(factoryMock.createWebServiceMessage(), response);
        factoryControl.replay();

        WebServiceMessage result = context.getResponse();
        assertEquals("Invalid response returned", response, result);
        factoryControl.verify();
    }

    public void testProperties() throws Exception {
        assertEquals("Invalid property names returned", 0, context.getPropertyNames().length);
        String name = "name";
        assertFalse("Property set", context.containsProperty(name));
        String value = "value";
        context.setProperty(name, value);
        assertTrue("Property not set", context.containsProperty(name));
        assertEquals("Invalid property names returned", Arrays.asList(new String[]{name}),
                Arrays.asList(context.getPropertyNames()));
        assertEquals("Invalid property value returned", value, context.getProperty(name));
        context.removeProperty(name);
        assertFalse("Property set", context.containsProperty(name));
        assertEquals("Invalid property names returned", 0, context.getPropertyNames().length);
    }

}