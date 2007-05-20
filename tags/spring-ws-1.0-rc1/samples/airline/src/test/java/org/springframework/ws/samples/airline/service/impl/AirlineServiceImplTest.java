/*
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
package org.springframework.ws.samples.airline.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.ws.samples.airline.dao.FlightDao;
import org.springframework.ws.samples.airline.dao.TicketDao;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.FrequentFlyer;
import org.springframework.ws.samples.airline.domain.Passenger;
import org.springframework.ws.samples.airline.domain.ServiceClass;
import org.springframework.ws.samples.airline.domain.Ticket;
import org.springframework.ws.samples.airline.security.FrequentFlyerSecurityService;
import org.springframework.ws.samples.airline.service.NoSeatAvailableException;
import org.springframework.ws.samples.airline.service.NoSuchFlightException;

public class AirlineServiceImplTest extends TestCase {

    private AirlineServiceImpl airlineService;

    private MockControl flightDaoControl;

    private FlightDao flightDaoMock;

    private MockControl ticketDaoControl;

    private TicketDao ticketDaoMock;

    private MockControl securityServiceControl;

    private FrequentFlyerSecurityService securityServiceMock;

    protected void setUp() throws Exception {
        airlineService = new AirlineServiceImpl();
        flightDaoControl = MockControl.createControl(FlightDao.class);
        flightDaoMock = (FlightDao) flightDaoControl.getMock();
        airlineService.setFlightDao(flightDaoMock);
        ticketDaoControl = MockControl.createControl(TicketDao.class);
        ticketDaoMock = (TicketDao) ticketDaoControl.getMock();
        airlineService.setTicketDao(ticketDaoMock);
        securityServiceControl = MockControl.createControl(FrequentFlyerSecurityService.class);
        securityServiceMock = (FrequentFlyerSecurityService) securityServiceControl.getMock();
        airlineService.setFrequentFlyerSecurityService(securityServiceMock);
    }

    protected void tearDown() throws Exception {
        flightDaoControl.verify();
        ticketDaoControl.verify();
        securityServiceControl.verify();
    }

    public void testBookFlight() throws Exception {
        String flightNumber = "AB1234";
        DateTime departureTime = new DateTime();
        Passenger passenger = new Passenger("John", "Doe");
        List passengers = new ArrayList();
        passengers.add(passenger);
        Flight flight = new Flight();
        flight.setNumber(flightNumber);
        flight.setSeatsAvailable(10);
        flightDaoControl.expectAndReturn(flightDaoMock.getFlight(flightNumber, departureTime), flight);
        flightDaoMock.update(flight);
        ticketDaoMock.save(null);
        ticketDaoControl.setMatcher(MockControl.ALWAYS_MATCHER);
        flightDaoControl.replay();
        ticketDaoControl.replay();
        securityServiceControl.replay();
        Ticket ticket = airlineService.bookFlight(flightNumber, departureTime, passengers);
        assertNotNull("Invalid ticket", ticket);
        assertEquals("Invalid flight", flight, ticket.getFlight());
        assertEquals("Invalid seats available", 9, flight.getSeatsAvailable());
        assertEquals("Invalid passengers count", 1, ticket.getPassengers().size());
    }

    public void testBookFlightFrequentFlyer() throws Exception {
        String flightNumber = "AB1234";
        DateTime departureTime = new DateTime();
        FrequentFlyer frequentFlyer = new FrequentFlyer("john", "changeme", "John", "Doe");
        List passengers = new ArrayList();
        passengers.add(frequentFlyer);
        Flight flight = new Flight();
        flight.setNumber(flightNumber);
        flight.setSeatsAvailable(1);
        flight.setMiles(10);
        securityServiceControl.expectAndReturn(securityServiceMock.getFrequentFlyer("john"), frequentFlyer);
        flightDaoControl.expectAndReturn(flightDaoMock.getFlight(flightNumber, departureTime), flight);
        flightDaoMock.update(flight);
        ticketDaoMock.save(null);
        ticketDaoControl.setMatcher(MockControl.ALWAYS_MATCHER);
        flightDaoControl.replay();
        ticketDaoControl.replay();
        securityServiceControl.replay();
        Ticket ticket = airlineService.bookFlight(flightNumber, departureTime, passengers);
        assertNotNull("Invalid ticket", ticket);
        assertEquals("Invalid flight", flight, ticket.getFlight());
        assertEquals("Invalid amount of miles", 10, frequentFlyer.getMiles());
    }

    public void testBookFlightNoSeatAvailable() throws Exception {
        String flightNumber = "AB1234";
        DateTime departureTime = new DateTime();
        List passengers = Collections.singletonList(new Passenger());
        Flight flight = new Flight();
        flightDaoControl.expectAndReturn(flightDaoMock.getFlight(flightNumber, departureTime), flight);
        flightDaoControl.replay();
        ticketDaoControl.replay();
        securityServiceControl.replay();
        try {
            airlineService.bookFlight(flightNumber, departureTime, passengers);
            fail("Should have thrown an NoSeatAvailableException");
        }
        catch (NoSeatAvailableException ex) {
        }
    }

    public void testBookFlightNoSuchFlight() throws Exception {
        String flightNumber = "AB1234";
        DateTime departureTime = new DateTime();
        List passengers = Collections.singletonList(new Passenger());
        flightDaoControl.expectAndReturn(flightDaoMock.getFlight(flightNumber, departureTime), null);
        flightDaoControl.replay();
        ticketDaoControl.replay();
        securityServiceControl.replay();
        try {
            airlineService.bookFlight(flightNumber, departureTime, passengers);
            fail("Should have thrown an NoSuchFlightException");
        }
        catch (NoSuchFlightException ex) {
        }
    }

    public void testGetFlights() throws Exception {
        String toCode = "to";
        String fromCode = "from";
        LocalDate departureDate = new LocalDate(2006, 1, 31);
        Flight flight = new Flight();
        List flights = new ArrayList();
        flights.add(flight);
        flightDaoControl.expectAndReturn(
                flightDaoMock.findFlights(fromCode, toCode, departureDate.toInterval(), ServiceClass.ECONOMY), flights);
        flightDaoControl.replay();
        ticketDaoControl.replay();
        securityServiceControl.replay();

        List result = airlineService.getFlights(fromCode, toCode, departureDate, ServiceClass.ECONOMY);
        assertEquals("Invalid result", flights, result);
    }

    public void testGetFlightsDefaultServiceClass() throws Exception {
        String toCode = "to";
        String fromCode = "from";
        LocalDate departureDate = new LocalDate(2006, 1, 31);
        Flight flight = new Flight();
        List flights = new ArrayList();
        flights.add(flight);
        flightDaoControl.expectAndReturn(
                flightDaoMock.findFlights(fromCode, toCode, departureDate.toInterval(), ServiceClass.ECONOMY), flights);
        flightDaoControl.replay();
        ticketDaoControl.replay();
        securityServiceControl.replay();

        List result = airlineService.getFlights(fromCode, toCode, departureDate, null);
        assertEquals("Invalid result", flights, result);
    }
}
