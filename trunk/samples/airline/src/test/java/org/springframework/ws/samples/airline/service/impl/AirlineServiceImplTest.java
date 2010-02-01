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

package org.springframework.ws.samples.airline.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import static org.easymock.EasyMock.*;
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

    private FlightDao flightDaoMock;

    private TicketDao ticketDaoMock;

    private FrequentFlyerSecurityService securityServiceMock;

    private String flightNumber;

    @Override
    protected void setUp() throws Exception {
        flightDaoMock = createMock(FlightDao.class);
        ticketDaoMock = createMock(TicketDao.class);
        airlineService = new AirlineServiceImpl(flightDaoMock, ticketDaoMock);
        securityServiceMock = createMock(FrequentFlyerSecurityService.class);
        airlineService.setFrequentFlyerSecurityService(securityServiceMock);
        flightNumber = "AB1234";
    }

    public void testBookFlight() throws Exception {
        DateTime departureTime = new DateTime();
        Passenger passenger = new Passenger("John", "Doe");
        List<Passenger> passengers = new ArrayList<Passenger>();
        passengers.add(passenger);
        Flight flight = new Flight();
        flight.setNumber(flightNumber);
        flight.setSeatsAvailable(10);
        expect(flightDaoMock.getFlight(flightNumber, departureTime)).andReturn(flight);
        expect(flightDaoMock.update(flight)).andReturn(flight);
        Ticket ticket = new Ticket();
        ticket.setFlight(flight);
        ticketDaoMock.save(isA(Ticket.class));

        replay(flightDaoMock, ticketDaoMock, securityServiceMock);

        ticket = airlineService.bookFlight(flightNumber, departureTime, passengers);
        assertNotNull("Invalid ticket", ticket);
        assertEquals("Invalid flight", flight, ticket.getFlight());
        assertEquals("Invalid seats available", 9, flight.getSeatsAvailable());
        assertEquals("Invalid passengers count", 1, ticket.getPassengers().size());

        verify(flightDaoMock, ticketDaoMock, securityServiceMock);
    }

    public void testBookFlightFrequentFlyer() throws Exception {
        DateTime departureTime = new DateTime();
        FrequentFlyer frequentFlyer = new FrequentFlyer("John", "Doe", "john", "changeme");
        List<Passenger> passengers = new ArrayList<Passenger>();
        passengers.add(frequentFlyer);
        Flight flight = new Flight();
        flight.setNumber(flightNumber);
        flight.setSeatsAvailable(1);
        flight.setMiles(10);
        expect(securityServiceMock.getFrequentFlyer("john")).andReturn(frequentFlyer);
        expect(flightDaoMock.getFlight(flightNumber, departureTime)).andReturn(flight);
        expect(flightDaoMock.update(flight)).andReturn(flight);
        ticketDaoMock.save(isA(Ticket.class));

        replay(flightDaoMock, ticketDaoMock, securityServiceMock);

        Ticket ticket = airlineService.bookFlight(flightNumber, departureTime, passengers);
        assertNotNull("Invalid ticket", ticket);
        assertEquals("Invalid flight", flight, ticket.getFlight());
        assertEquals("Invalid amount of miles", 10, frequentFlyer.getMiles());

        verify(flightDaoMock, ticketDaoMock, securityServiceMock);
    }

    public void testBookFlightNoSeatAvailable() throws Exception {
        DateTime departureTime = new DateTime();
        List<Passenger> passengers = Collections.singletonList(new Passenger());
        Flight flight = new Flight();
        flight.setNumber("AB1234");
        flight.setDepartureTime(new DateTime());
        expect(flightDaoMock.getFlight(flightNumber, departureTime)).andReturn(flight);

        replay(flightDaoMock, ticketDaoMock, securityServiceMock);

        try {
            airlineService.bookFlight(flightNumber, departureTime, passengers);
            fail("Should have thrown an NoSeatAvailableException");
        }
        catch (NoSeatAvailableException ex) {
        }

        verify(flightDaoMock, ticketDaoMock, securityServiceMock);
    }

    public void testBookFlightNoSuchFlight() throws Exception {
        String flightNumber = "AB1234";
        DateTime departureTime = new DateTime();
        List<Passenger> passengers = Collections.singletonList(new Passenger());
        expect(flightDaoMock.getFlight(flightNumber, departureTime)).andReturn(null);

        replay(flightDaoMock, ticketDaoMock, securityServiceMock);
        try {
            airlineService.bookFlight(flightNumber, departureTime, passengers);
            fail("Should have thrown an NoSuchFlightException");
        }
        catch (NoSuchFlightException ex) {
        }

        verify(flightDaoMock, ticketDaoMock, securityServiceMock);
    }

    public void testGetFlights() throws Exception {
        LocalDate departureDate = new LocalDate(2006, 1, 31);
        Flight flight = new Flight();
        List<Flight> flights = new ArrayList<Flight>();
        flights.add(flight);
        String toCode = "to";
        String fromCode = "from";

        expect(flightDaoMock.findFlights(fromCode, toCode, departureDate.toInterval(), ServiceClass.ECONOMY))
                .andReturn(flights);

        replay(flightDaoMock, ticketDaoMock, securityServiceMock);

        List<Flight> result = airlineService.getFlights(fromCode, toCode, departureDate, ServiceClass.ECONOMY);
        assertEquals("Invalid result", flights, result);

        verify(flightDaoMock, ticketDaoMock, securityServiceMock);
    }

    public void testGetFlightsDefaultServiceClass() throws Exception {
        LocalDate departureDate = new LocalDate(2006, 1, 31);
        Flight flight = new Flight();
        List<Flight> flights = new ArrayList<Flight>();
        flights.add(flight);
        String toCode = "to";
        String fromCode = "from";

        expect(flightDaoMock.findFlights(fromCode, toCode, departureDate.toInterval(), ServiceClass.ECONOMY))
                .andReturn(flights);

        replay(flightDaoMock, ticketDaoMock, securityServiceMock);

        List<Flight> result = airlineService.getFlights(fromCode, toCode, departureDate, null);
        assertEquals("Invalid result", flights, result);

        verify(flightDaoMock, ticketDaoMock, securityServiceMock);

    }
}
