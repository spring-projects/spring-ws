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

package org.springframework.ws.samples.airline.ws;

import junit.framework.TestCase;
import static org.easymock.EasyMock.createMock;
import org.springframework.oxm.Marshaller;
import org.springframework.ws.samples.airline.service.AirlineService;

public class XPathAirlineEndpointTest extends TestCase {

    private XPathAirlineEndpoint endpoint;

    private AirlineService airlineServiceMock;

    private Marshaller marshallerMock;

    protected void setUp() throws Exception {
        airlineServiceMock = createMock(AirlineService.class);
        marshallerMock = createMock(Marshaller.class);
        endpoint = new XPathAirlineEndpoint(airlineServiceMock, marshallerMock);
    }

    public void testGetFlights() throws Exception {
        endpoint.getFlights("ABC", "DEF", "2006-01-31Z", "");
    }
}