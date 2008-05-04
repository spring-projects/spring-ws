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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;

import junit.framework.TestCase;
import static org.easymock.EasyMock.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.springframework.ws.samples.airline.domain.Airport;
import org.springframework.ws.samples.airline.domain.FrequentFlyer;
import org.springframework.ws.samples.airline.domain.Passenger;
import org.springframework.ws.samples.airline.schema.BookFlightRequest;
import org.springframework.ws.samples.airline.schema.Flight;
import org.springframework.ws.samples.airline.schema.GetFlightsRequest;
import org.springframework.ws.samples.airline.schema.GetFlightsResponse;
import org.springframework.ws.samples.airline.schema.Name;
import org.springframework.ws.samples.airline.schema.ObjectFactory;
import org.springframework.ws.samples.airline.schema.ServiceClass;
import org.springframework.ws.samples.airline.schema.Ticket;
import org.springframework.ws.samples.airline.service.AirlineService;

public class MarshallingAirlineEndpointTest extends TestCase {

    private MarshallingAirlineEndpoint endpoint;

    private AirlineService airlineServiceMock;

    private DatatypeFactory datatypeFactory;

    private ObjectFactory objectFactory;

    protected void setUp() throws Exception {
        airlineServiceMock = createMock(AirlineService.class);
        endpoint = new MarshallingAirlineEndpoint(airlineServiceMock);
        datatypeFactory = DatatypeFactory.newInstance();
        objectFactory = new ObjectFactory();
    }

    public void testGetFlights() throws Exception {
        GetFlightsRequest request = objectFactory.createGetFlightsRequest();
        request.setDepartureDate(
                datatypeFactory.newXMLGregorianCalendarDate(2007, 6, 13, DatatypeConstants.FIELD_UNDEFINED));
        request.setFrom("ABC");
        request.setTo("DEF");
        request.setServiceClass(ServiceClass.FIRST);

        org.springframework.ws.samples.airline.domain.Flight domainFlight = createDomainFlight();

        expect(airlineServiceMock.getFlights("ABC", "DEF", new LocalDate(2007, 6, 13),
                org.springframework.ws.samples.airline.domain.ServiceClass.FIRST))
                .andReturn(Collections.singletonList(domainFlight));

        replay(airlineServiceMock);

        GetFlightsResponse response = endpoint.getFlights(request);
        assertEquals("Invalid amount of flights received", 1, response.getFlight().size());
        Flight schemaFlight = response.getFlight().get(0);
        verifySchemaFlight(schemaFlight);

        verify(airlineServiceMock);
    }

    private void verifySchemaFlight(Flight schemaFlight) {
        assertEquals("Invalid number", "ABC1234", schemaFlight.getNumber());
        assertEquals("Invalid departure time", datatypeFactory.newXMLGregorianCalendar(2007, 6, 13, 12, 0, 0, 0, 0),
                schemaFlight.getDepartureTime());
        assertEquals("Invalid from code", "ABC", schemaFlight.getFrom().getCode());
        assertEquals("Invalid from name", "ABC Airport", schemaFlight.getFrom().getName());
        assertEquals("Invalid from city", "ABC City", schemaFlight.getFrom().getCity());
        assertEquals("Invalid arrival time", datatypeFactory.newXMLGregorianCalendar(2007, 6, 13, 14, 0, 0, 0, 0),
                schemaFlight.getArrivalTime());
        assertEquals("Invalid to code", "DEF", schemaFlight.getTo().getCode());
        assertEquals("Invalid to name", "DEF Airport", schemaFlight.getTo().getName());
        assertEquals("Invalid to city", "DEF City", schemaFlight.getTo().getCity());
        assertEquals("Invalid service class", ServiceClass.FIRST, schemaFlight.getServiceClass());
    }

    private org.springframework.ws.samples.airline.domain.Flight createDomainFlight() {
        org.springframework.ws.samples.airline.domain.Flight domainFlight =
                new org.springframework.ws.samples.airline.domain.Flight();
        domainFlight.setNumber("ABC1234");
        domainFlight.setDepartureTime(new DateTime(2007, 6, 13, 12, 0, 0, 0, DateTimeZone.UTC));
        domainFlight.setFrom(new Airport("ABC", "ABC Airport", "ABC City"));
        domainFlight.setArrivalTime(new DateTime(2007, 6, 13, 14, 0, 0, 0, DateTimeZone.UTC));
        domainFlight.setTo(new Airport("DEF", "DEF Airport", "DEF City"));
        domainFlight.setServiceClass(org.springframework.ws.samples.airline.domain.ServiceClass.FIRST);
        return domainFlight;
    }

    public void testBookFlightPassenger() throws Exception {
        BookFlightRequest request = objectFactory.createBookFlightRequest();
        request.setDepartureTime(datatypeFactory.newXMLGregorianCalendar(2007, 6, 13, 12, 0, 0, 0, 0));
        request.setFlightNumber("ABC1234");
        Name passengerName = new Name();
        passengerName.setFirst("John");
        passengerName.setLast("Doe");
        BookFlightRequest.Passengers passengers = new BookFlightRequest.Passengers();
        passengers.getPassengerOrUsername().add(passengerName);
        request.setPassengers(passengers);

        Passenger domainPassenger = new Passenger("John", "Doe");

        org.springframework.ws.samples.airline.domain.Ticket domainTicket =
                new org.springframework.ws.samples.airline.domain.Ticket(42L);
        domainTicket.setFlight(createDomainFlight());
        domainTicket.setIssueDate(new LocalDate(2007, 6, 13));
        domainTicket.setPassengers(Collections.singleton(domainPassenger));

        expect(airlineServiceMock.bookFlight("ABC1234", new DateTime(2007, 6, 13, 12, 0, 0, 0, DateTimeZone.UTC),
                Collections.singletonList(domainPassenger))).andReturn(domainTicket);

        replay(airlineServiceMock);

        JAXBElement<Ticket> response = endpoint.bookFlight(request);
        Ticket schemaTicket = response.getValue();
        assertEquals("Invalid id", 42L, schemaTicket.getId());
        assertEquals("Invalid issue date",
                datatypeFactory.newXMLGregorianCalendarDate(2007, 6, 13, DatatypeConstants.FIELD_UNDEFINED),
                schemaTicket.getIssueDate());
        assertEquals("Invalid amount of passengers", 1, schemaTicket.getPassengers().getPassenger().size());
        Name schemaPassenger = schemaTicket.getPassengers().getPassenger().get(0);
        assertEquals("Invalid passenger first name", "John", schemaPassenger.getFirst());
        assertEquals("Invalid passenger first name", "Doe", schemaPassenger.getLast());
        verifySchemaFlight(schemaTicket.getFlight());

        verify(airlineServiceMock);
    }

    public void testBookFlightFrequentFlyer() throws Exception {
        BookFlightRequest request = objectFactory.createBookFlightRequest();
        request.setDepartureTime(datatypeFactory.newXMLGregorianCalendar(2007, 6, 13, 12, 0, 0, 0, 0));
        request.setFlightNumber("ABC1234");
        BookFlightRequest.Passengers passengers = new BookFlightRequest.Passengers();
        passengers.getPassengerOrUsername().add("john");
        request.setPassengers(passengers);

        FrequentFlyer domainFrequentFlyer = new FrequentFlyer("John", "Doe", "john", "changeme");
        Set<Passenger> domainPassengers = new HashSet<Passenger>();
        domainPassengers.add(domainFrequentFlyer);

        org.springframework.ws.samples.airline.domain.Ticket domainTicket =
                new org.springframework.ws.samples.airline.domain.Ticket(42L);
        domainTicket.setFlight(createDomainFlight());
        domainTicket.setIssueDate(new LocalDate(2007, 6, 13));
        domainTicket.setPassengers(domainPassengers);

        List<Passenger> domainPassengerList = new ArrayList<Passenger>(domainPassengers);
        expect(airlineServiceMock.bookFlight("ABC1234", new DateTime(2007, 6, 13, 12, 0, 0, 0, DateTimeZone.UTC),
                domainPassengerList)).andReturn(domainTicket);

        replay(airlineServiceMock);

        JAXBElement<Ticket> response = endpoint.bookFlight(request);
        Ticket schemaTicket = response.getValue();
        assertEquals("Invalid id", 42L, schemaTicket.getId());
        assertEquals("Invalid issue date",
                datatypeFactory.newXMLGregorianCalendarDate(2007, 6, 13, DatatypeConstants.FIELD_UNDEFINED),
                schemaTicket.getIssueDate());
        assertEquals("Invalid amount of passengers", 1, schemaTicket.getPassengers().getPassenger().size());
        Name schemaPassenger = schemaTicket.getPassengers().getPassenger().get(0);
        assertEquals("Invalid passenger first name", "John", schemaPassenger.getFirst());
        assertEquals("Invalid passenger first name", "Doe", schemaPassenger.getLast());
        verifySchemaFlight(schemaTicket.getFlight());

        verify(airlineServiceMock);
    }

}