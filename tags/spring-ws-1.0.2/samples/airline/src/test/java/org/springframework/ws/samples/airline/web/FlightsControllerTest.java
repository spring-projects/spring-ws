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

package org.springframework.ws.samples.airline.web;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import static org.easymock.EasyMock.*;
import org.joda.time.LocalDate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.ServiceClass;
import org.springframework.ws.samples.airline.service.AirlineService;

public class FlightsControllerTest extends TestCase {

    private FlightsController flightsController;

    private AirlineService airlineServiceMock;

    protected void setUp() throws Exception {
        airlineServiceMock = createMock(AirlineService.class);
        flightsController = new FlightsController(airlineServiceMock);
    }

    public void testFlightList() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String from = "AMS";
        request.setParameter("from", from);
        String to = "VCE";
        request.setParameter("to", to);
        LocalDate departureDate = new LocalDate();
        request.setParameter("departureDate", departureDate.toString());
        ServiceClass serviceClass = ServiceClass.FIRST;
        request.setParameter("serviceClass", serviceClass.toString());
        MockHttpServletResponse response = new MockHttpServletResponse();
        List<Flight> flights = new ArrayList<Flight>();
        flights.add(new Flight());
        expect(airlineServiceMock.getFlights(from, to, departureDate, serviceClass)).andReturn(flights);

        replay(airlineServiceMock);

        ModelAndView mav = flightsController.flightList(request, response);
        assertNotNull("No ModelAndView returned", mav);
        assertEquals("Invalid view name", "flights", mav.getViewName());
        assertTrue("No flights in ModelAndView", mav.getModel().containsKey("flights"));
        verify(airlineServiceMock);
    }
}