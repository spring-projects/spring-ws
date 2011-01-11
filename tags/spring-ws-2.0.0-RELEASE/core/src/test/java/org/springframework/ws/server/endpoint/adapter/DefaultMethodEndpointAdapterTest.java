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

package org.springframework.ws.server.endpoint.adapter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.springframework.core.MethodParameter;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/** @author Arjen Poutsma */
public class DefaultMethodEndpointAdapterTest {

    private DefaultMethodEndpointAdapter adapter;

    private MethodArgumentResolver argumentResolver1;

    private MethodArgumentResolver argumentResolver2;

    private MethodReturnValueHandler returnValueHandler;

    private MethodEndpoint supportedEndpoint;

    private MethodEndpoint unsupportedEndpoint;

    private MethodEndpoint exceptionEndpoint;

    private String supportedArgument;

    @Before
    public void setUp() throws Exception {
        adapter = new DefaultMethodEndpointAdapter();
        argumentResolver1 = createMock("stringResolver", MethodArgumentResolver.class);
        argumentResolver2 = createMock("intResolver", MethodArgumentResolver.class);
        returnValueHandler = createMock(MethodReturnValueHandler.class);
        adapter.setMethodArgumentResolvers(Arrays.asList(argumentResolver1, argumentResolver2));
        adapter.setMethodReturnValueHandlers(Collections.singletonList(returnValueHandler));
        supportedEndpoint = new MethodEndpoint(this, "supported", String.class, Integer.class);
        unsupportedEndpoint = new MethodEndpoint(this, "unsupported", String.class);
        exceptionEndpoint = new MethodEndpoint(this, "exception", String.class);
    }

    @Test
    public void initDefaultStrategies() throws Exception {
        adapter = new DefaultMethodEndpointAdapter();
        adapter.setBeanClassLoader(DefaultMethodEndpointAdapterTest.class.getClassLoader());
        adapter.afterPropertiesSet();

        assertFalse("No default MethodArgumentResolvers loaded", adapter.getMethodArgumentResolvers().isEmpty());
        assertFalse("No default MethodReturnValueHandlers loaded", adapter.getMethodReturnValueHandlers().isEmpty());
    }

    @Test
    public void supportsSupported() throws Exception {
        expect(argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(true);
        expect(argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(false);
        expect(argumentResolver2.supportsParameter(isA(MethodParameter.class))).andReturn(true);
        expect(returnValueHandler.supportsReturnType(isA(MethodParameter.class))).andReturn(true);

        replay(argumentResolver1, argumentResolver2, returnValueHandler);

        boolean result = adapter.supports(supportedEndpoint);
        assertTrue("adapter does not support method", result);

        verify(argumentResolver1, argumentResolver2, returnValueHandler);
    }

    @Test
    public void supportsUnsupportedParameter() throws Exception {
        expect(argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(false);
        expect(argumentResolver2.supportsParameter(isA(MethodParameter.class))).andReturn(false);

        replay(argumentResolver1, argumentResolver2, returnValueHandler);


        boolean result = adapter.supports(unsupportedEndpoint);
        assertFalse("adapter does not support method", result);

        verify(argumentResolver1, argumentResolver2, returnValueHandler);
    }

    @Test
    public void supportsUnsupportedReturnType() throws Exception {
        expect(argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(true);
        expect(returnValueHandler.supportsReturnType(isA(MethodParameter.class))).andReturn(false);

        replay(argumentResolver1, argumentResolver2, returnValueHandler);

        boolean result = adapter.supports(unsupportedEndpoint);
        assertFalse("adapter does not support method", result);

        verify(argumentResolver1, argumentResolver2, returnValueHandler);
    }

    @Test
    public void invokeSupported() throws Exception {
        MockWebServiceMessage request = new MockWebServiceMessage("<root xmlns='http://springframework.org'/>");
        MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

        String value = "Foo";

        // arg 0
        expect(argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(true);
        expect(argumentResolver1.resolveArgument(eq(messageContext), isA(MethodParameter.class))).andReturn(value);

        // arg 1
        expect(argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(false);
        expect(argumentResolver2.supportsParameter(isA(MethodParameter.class))).andReturn(true);
        expect(argumentResolver2.resolveArgument(eq(messageContext), isA(MethodParameter.class))).andReturn(new Integer(42));

        expect(returnValueHandler.supportsReturnType(isA(MethodParameter.class))).andReturn(true);
        returnValueHandler.handleReturnValue(eq(messageContext), isA(MethodParameter.class), eq(value));

        replay(argumentResolver1, argumentResolver2, returnValueHandler);

        adapter.invoke(messageContext, supportedEndpoint);
        assertEquals("Invalid argument passed", value, supportedArgument);

        verify(argumentResolver1, argumentResolver2, returnValueHandler);
    }

    @Test
    public void invokeException() throws Exception {
        MockWebServiceMessage request = new MockWebServiceMessage("<root xmlns='http://springframework.org'/>");
        MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

        String value = "Foo";

        expect(argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(true);
        expect(argumentResolver1.resolveArgument(eq(messageContext), isA(MethodParameter.class))).andReturn(value);


        replay(argumentResolver1, argumentResolver2, returnValueHandler);

        try {
            adapter.invoke(messageContext, exceptionEndpoint);
            fail("IOException expected");
        }
        catch (IOException expected) {
            // expected
        }
        assertEquals("Invalid argument passed", value, supportedArgument);

        verify(argumentResolver1, argumentResolver2, returnValueHandler);
    }

    public String supported(String s, Integer i) {
        supportedArgument = s;
        return s;

    }

    public String unsupported(String s) {
        return s;
    }

    public String exception(String s) throws IOException {
        supportedArgument = s;
        throw new IOException(s);
    }
}
