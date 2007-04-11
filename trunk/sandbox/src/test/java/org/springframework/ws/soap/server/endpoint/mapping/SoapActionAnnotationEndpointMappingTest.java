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
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.soap.server.endpoint.annotation.SoapAction;

public class SoapActionAnnotationEndpointMappingTest extends TestCase {

    private StaticApplicationContext applicationContext;

    private SoapActionAnnotationEndpointMapping mapping;

    protected void setUp() throws Exception {
        applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("mapping", SoapActionAnnotationEndpointMapping.class);
        applicationContext.registerSingleton("endpoint", MyEndpoint.class);
        applicationContext.refresh();
        mapping = (SoapActionAnnotationEndpointMapping) applicationContext.getBean("mapping");
    }

    public void testIt() {

    }

    @Endpoint
    private static class MyEndpoint {

        @SoapAction("http://springframework.org/spring-ws/action")
        public void handleMessage() {

        }
    }
}