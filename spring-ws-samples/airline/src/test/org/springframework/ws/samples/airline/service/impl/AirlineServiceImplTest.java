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
package org.springframework.ws.samples.airline.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.ws.samples.airline.dao.CustomerDao;
import org.springframework.ws.samples.airline.dao.FlightDao;
import org.springframework.ws.samples.airline.dao.TicketDao;
import org.springframework.ws.samples.airline.domain.Customer;
import org.springframework.ws.samples.airline.domain.Flight;

public class AirlineServiceImplTest extends TestCase {

    private AirlineServiceImpl airlineService;

    private MockControl customerDaoControl;

    private CustomerDao customerDaoMock;

    private MockControl flightDaoControl;

    private FlightDao flightDaoMock;

    private MockControl ticketDaoControl;

    private TicketDao ticketDaoMock;

    protected void setUp() throws Exception {
        airlineService = new AirlineServiceImpl();
        customerDaoControl = MockControl.createControl(CustomerDao.class);
        customerDaoMock = (CustomerDao) customerDaoControl.getMock();
        airlineService.setCustomerDao(customerDaoMock);
        flightDaoControl = MockControl.createControl(FlightDao.class);
        flightDaoMock = (FlightDao) flightDaoControl.getMock();
        airlineService.setFlightDao(flightDaoMock);
        ticketDaoControl = MockControl.createControl(TicketDao.class);
        ticketDaoMock = (TicketDao) ticketDaoControl.getMock();
        airlineService.setTicketDao(ticketDaoMock);
    }

    public void testBookFlight() {
        Flight flight = new Flight();
        flightDaoControl
                .expectAndReturn(flightDaoMock.getFlights("1234", null, null), Collections.singletonList(flight));
        Customer customer = new Customer();
        customerDaoControl.expectAndReturn(customerDaoMock.getCustomer(42L), customer);
        ticketDaoMock.insertTicket(null);
        ticketDaoControl.setMatcher(MockControl.ALWAYS_MATCHER);

        customerDaoControl.replay();
        flightDaoControl.replay();
        ticketDaoControl.replay();

        airlineService.bookFlight("1234", 42L);

        customerDaoControl.verify();
        flightDaoControl.verify();
        ticketDaoControl.verify();
    }

    public void testGetFlightsInPeriod() {
        String number = "number";
        Calendar startPeriod = Calendar.getInstance();
        Calendar endPeriod = Calendar.getInstance();
        Flight flight = new Flight();
        List flights = new ArrayList();
        flights.add(flight);
        flightDaoControl.expectAndReturn(flightDaoMock.getFlights(number, startPeriod, endPeriod), flights);

        customerDaoControl.replay();
        flightDaoControl.replay();
        ticketDaoControl.replay();

        List result = airlineService.getFlightsInPeriod(number, startPeriod, endPeriod);
        assertEquals("Invalid result", flights, result);

        customerDaoControl.verify();
        flightDaoControl.verify();
        ticketDaoControl.verify();

    }

}
