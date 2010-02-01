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

package org.springframework.ws.server.endpoint;

import java.lang.reflect.Method;

import junit.framework.TestCase;

public class MethodEndpointTest extends TestCase {

    private MethodEndpoint endpoint;

    private boolean myMethodInvoked;

    private Method method;

    @Override
    protected void setUp() throws Exception {
        myMethodInvoked = false;
        method = getClass().getMethod("myMethod", new Class[]{String.class});
        endpoint = new MethodEndpoint(this, method);
    }

    public void testGetters() throws Exception {
        assertEquals("Invalid bean", this, endpoint.getBean());
        assertEquals("Invalid bean", method, endpoint.getMethod());
    }

    public void testInvoke() throws Exception {
        assertFalse("Method invoked before invocation", myMethodInvoked);
        endpoint.invoke(new Object[]{"arg"});
        assertTrue("Method invoked before invocation", myMethodInvoked);
    }

    public void testEquals() throws Exception {
        assertEquals("Not equal", endpoint, endpoint);
        assertEquals("Not equal", new MethodEndpoint(this, method), endpoint);
        Method otherMethod = getClass().getMethod("testEquals", new Class[0]);
        assertFalse("Equal", new MethodEndpoint(this, otherMethod).equals(endpoint));
    }

    public void testHashCode() throws Exception {
        assertEquals("Not equal", new MethodEndpoint(this, method).hashCode(), endpoint.hashCode());
        Method otherMethod = getClass().getMethod("testEquals", new Class[0]);
        assertFalse("Equal", new MethodEndpoint(this, otherMethod).hashCode() == endpoint.hashCode());
    }

    public void myMethod(String arg) {
        assertEquals("Invalid argument", "arg", arg);
        myMethodInvoked = true;
    }

    public void testToString() throws Exception {
        assertNotNull("Na valid toString", endpoint.toString());
    }
}
