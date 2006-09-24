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
package org.springframework.ws.samples.airline.ws;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.schema.GetFlightsRequest;
import org.springframework.ws.samples.airline.schema.GetFlightsResponse;
import org.springframework.ws.samples.airline.schema.impl.GetFlightsRequestImpl;
import org.springframework.ws.samples.airline.service.AirlineService;

public class GetFlightsEndpointTest extends TestCase {

    private GetFlightsEndpoint endpoint;

    private MockControl serviceControl;

    private AirlineService serviceMock;

    protected void setUp() throws Exception {
        endpoint = new GetFlightsEndpoint();
        serviceControl = MockControl.createControl(AirlineService.class);
        serviceMock = (AirlineService) serviceControl.getMock();
        endpoint.setAirlineService(serviceMock);
    }

    public void testInvoke() throws Exception {
        Calendar startOfPeriod = Calendar.getInstance();
        Calendar endOfPeriod = Calendar.getInstance();
        GetFlightsRequest request = new GetFlightsRequestImpl();
        String number = "number";
        request.setFlightNumber(number);
        request.setStartOfPeriod(startOfPeriod);
        request.setEndOfPeriod(endOfPeriod);
        List flights = new ArrayList();
        Flight flight = new Flight();
        flight.setNumber(number);
        flights.add(flight);
        serviceControl.expectAndReturn(serviceMock.getFlightsInPeriod(number, startOfPeriod, endOfPeriod), flights);
        serviceControl.replay();
        GetFlightsResponse response = (GetFlightsResponse) endpoint.invokeInternal(request);
        serviceControl.verify();
        assertNotNull("Response is null", response);
        assertEquals("Invalid amount of flights in response", 1, response.getFlight().size());
        org.springframework.ws.samples.airline.schema.Flight responseFlight =
                (org.springframework.ws.samples.airline.schema.Flight) response.getFlight().get(0);
        assertEquals("Invalid flight number on flight", number, responseFlight.getNumber());
    }

}
