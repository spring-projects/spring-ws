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

package org.springframework.ws.server.endpoint.mapping;

import junit.framework.TestCase;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;

public class PayloadRootAnnotationMethodEndpointMappingTest extends TestCase {

    private PayloadRootAnnotationMethodEndpointMapping mapping;

    private StaticApplicationContext applicationContext;

    protected void setUp() throws Exception {
        applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("mapping", PayloadRootAnnotationMethodEndpointMapping.class);
        applicationContext.registerSingleton("endpoint", MyEndpoint.class);
        applicationContext.registerSingleton("other", OtherBean.class);
        applicationContext.refresh();
        mapping = (PayloadRootAnnotationMethodEndpointMapping) applicationContext.getBean("mapping");
    }

    public void testRegistration() throws NoSuchMethodException {
        MethodEndpoint endpoint = mapping.lookupEndpoint("{http://springframework.org/spring-ws}Request");
        assertNotNull("MethodEndpoint not registered", endpoint);
        MethodEndpoint expected = new MethodEndpoint(applicationContext.getBean("endpoint"), "doIt", new Class[0]);
        assertEquals("Invalid endpoint registered", expected, endpoint);

        assertNull("Invalid endpoint registered",
                mapping.lookupEndpoint("{http://springframework.org/spring-ws}Request2"));
    }

    @Endpoint
    private static class MyEndpoint {

        @PayloadRoot(localPart = "Request", namespace = "http://springframework.org/spring-ws")
        public void doIt() {

        }

    }

    private static class OtherBean {

        @PayloadRoot(localPart = "Request2", namespace = "http://springframework.org/spring-ws")
        public void doIt() {

        }

    }

}