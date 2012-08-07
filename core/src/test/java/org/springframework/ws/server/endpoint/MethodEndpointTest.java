/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server.endpoint;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MethodEndpointTest {

    private MethodEndpoint endpoint;

    private boolean myMethodInvoked;

    private Method method;

    @Before
    public void setUp() throws Exception {
        myMethodInvoked = false;
        method = getClass().getMethod("myMethod", String.class);
        endpoint = new MethodEndpoint(this, method);
    }

    @Test
    public void testGetters() throws Exception {
        Assert.assertEquals("Invalid bean", this, endpoint.getBean());
        Assert.assertEquals("Invalid bean", method, endpoint.getMethod());
    }

    @Test
    public void testInvoke() throws Exception {
        Assert.assertFalse("Method invoked before invocation", myMethodInvoked);
        endpoint.invoke("arg");
        Assert.assertTrue("Method invoked before invocation", myMethodInvoked);
    }

    @Test
    public void testEquals() throws Exception {
        Assert.assertEquals("Not equal", endpoint, endpoint);
        Assert.assertEquals("Not equal", new MethodEndpoint(this, method), endpoint);
        Method otherMethod = getClass().getMethod("testEquals");
        Assert.assertFalse("Equal", new MethodEndpoint(this, otherMethod).equals(endpoint));
    }

    @Test
    public void testHashCode() throws Exception {
        Assert.assertEquals("Not equal", new MethodEndpoint(this, method).hashCode(), endpoint.hashCode());
        Method otherMethod = getClass().getMethod("testEquals");
        Assert.assertFalse("Equal", new MethodEndpoint(this, otherMethod).hashCode() == endpoint.hashCode());
    }

    @Test
    public void testToString() throws Exception {
        Assert.assertNotNull("No valid toString", endpoint.toString());
    }

    public void myMethod(String arg) {
        Assert.assertEquals("Invalid argument", "arg", arg);
        myMethodInvoked = true;
    }
}
