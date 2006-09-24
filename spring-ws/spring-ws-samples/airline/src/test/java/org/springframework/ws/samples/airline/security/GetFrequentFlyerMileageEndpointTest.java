/*
 * Copyright (c) 2006, Your Corporation. All Rights Reserved.
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

package org.springframework.ws.samples.airline.security;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.jdom.Element;

import org.springframework.ws.samples.airline.service.AirlineService;

public class GetFrequentFlyerMileageEndpointTest extends TestCase {

    private GetFrequentFlyerMileageEndpoint endpoint;

    private MockControl control;

    private AirlineService mock;

    protected void setUp() throws Exception {
        endpoint = new GetFrequentFlyerMileageEndpoint();
        control = MockControl.createControl(AirlineService.class);
        mock = (AirlineService) control.getMock();
        endpoint.setAirlineService(mock);
        endpoint.afterPropertiesSet();

    }

    public void testInvokeInternal() throws Exception {
        control.expectAndReturn(mock.getFrequentFlyerMileage(), 42);
        control.replay();
        Element element = endpoint.invokeInternal(null);
        assertNotNull("No element returned", element);
        assertEquals("Invalid local name", "GetFrequentFlyerMileageResponse", element.getName());
        assertEquals("Invalid namespace", "http://www.springframework.org/spring-ws/samples/airline/schemas",
                element.getNamespaceURI());
        assertEquals("Invalid result", "42", element.getTextNormalize());
        control.verify();
    }
}