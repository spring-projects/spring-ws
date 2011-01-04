/*
 * Copyright 2005-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.samples.airline.ws;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
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
import org.springframework.ws.samples.airline.schema.support.SchemaConversionUtils;
import org.springframework.ws.samples.airline.service.AirlineService;
import org.springframework.ws.samples.airline.service.NoSeatAvailableException;
import org.springframework.ws.samples.airline.service.NoSuchFlightException;
import org.springframework.ws.samples.airline.service.NoSuchFrequentFlyerException;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 * Endpoint that handles the Airline Web Service messages using JAXB2 marshalling.
 * <p/>
 * This endpoint contains exactly the same logic as the {@link XPathAirlineEndpoint}, and is only provided as an
 * example. Typically, you will not have two endpoints with the same logic.
 *
 * @author Arjen Poutsma
 */
@Endpoint
public class MarshallingAirlineEndpoint implements AirlineWebServiceConstants {

    private static final Log logger = LogFactory.getLog(MarshallingAirlineEndpoint.class);

    private final AirlineService airlineService;

    private ObjectFactory objectFactory = new ObjectFactory();

    @Autowired
    public MarshallingAirlineEndpoint(AirlineService airlineService) {
        Assert.notNull(airlineService, "airlineService must not be null");
        this.airlineService = airlineService;
    }

    /**
     * This endpoint method uses marshalling to handle message with a <code>&lt;GetFlightsRequest&gt;</code> payload.
     *
     * @param request the JAXB2 representation of a <code>&lt;GetFlightsRequest&gt;</code>
     */
    @PayloadRoot(localPart = GET_FLIGHTS_REQUEST, namespace = MESSAGES_NAMESPACE)
    public GetFlightsResponse getFlights(GetFlightsRequest request) throws DatatypeConfigurationException {
        if (logger.isDebugEnabled()) {
            logger.debug("Received GetFlightsRequest '" + request.getFrom() + "' to '" + request.getTo() + "' on " +
                    request.getDepartureDate());
        }
        List<Flight> flights = getSchemaFlights(request.getFrom(), request.getTo(), request.getDepartureDate(),
                request.getServiceClass());
        GetFlightsResponse response = new GetFlightsResponse();
        for (Flight flight : flights) {
            response.getFlight().add(flight);
        }
        return response;
    }

    /** Converts between the domain and schema types. */
    private List<Flight> getSchemaFlights(String from,
                                          String to,
                                          XMLGregorianCalendar xmlDepartureDate,
                                          ServiceClass xmlServiceClass) throws DatatypeConfigurationException {
        LocalDate domainDepartureDate = SchemaConversionUtils.toLocalDate(xmlDepartureDate);
        org.springframework.ws.samples.airline.domain.ServiceClass domainServiceClass =
                SchemaConversionUtils.toDomainType(xmlServiceClass);
        List<org.springframework.ws.samples.airline.domain.Flight> domainFlights =
                airlineService.getFlights(from, to, domainDepartureDate, domainServiceClass);
        return SchemaConversionUtils.toSchemaType(domainFlights);
    }

    /**
     * This endpoint method uses marshalling to handle message with a <code>&lt;BookFlightRequest&gt;</code> payload.
     *
     * @param request the JAXB2 representation of a <code>&lt;BookFlightRequest&gt;</code>
     * @return the JAXB2 representation of a <code>&lt;BookFlightResponse&gt;</code>
     */
    @PayloadRoot(localPart = BOOK_FLIGHT_REQUEST, namespace = MESSAGES_NAMESPACE)
    public JAXBElement<Ticket> bookFlight(BookFlightRequest request) throws NoSeatAvailableException,
            DatatypeConfigurationException, NoSuchFlightException, NoSuchFrequentFlyerException {
        if (logger.isDebugEnabled()) {
            logger.debug("Received BookingFlightRequest '" + request.getFlightNumber() + "' on '" +
                    request.getDepartureTime() + "' for " + request.getPassengers().getPassengerOrUsername());
        }
        Ticket ticket = bookSchemaFlight(request.getFlightNumber(), request.getDepartureTime(),
                request.getPassengers().getPassengerOrUsername());
        return objectFactory.createBookFlightResponse(ticket);
    }

    /** Converts between the domain and schema types. */
    private Ticket bookSchemaFlight(String flightNumber,
                                    XMLGregorianCalendar xmlDepartureTime,
                                    List<Object> passengerOrUsernameList) throws NoSeatAvailableException,
            NoSuchFlightException, NoSuchFrequentFlyerException, DatatypeConfigurationException {
        DateTime departureTime = SchemaConversionUtils.toDateTime(xmlDepartureTime);
        List<Passenger> passengers = new ArrayList<Passenger>(passengerOrUsernameList.size());
        for (Iterator<Object> iterator = passengerOrUsernameList.iterator(); iterator.hasNext();) {
            Object passengerOrUsername = iterator.next();
            if (passengerOrUsername instanceof Name) {
                Name passengerName = (Name) passengerOrUsername;
                Passenger passenger = new Passenger(passengerName.getFirst(), passengerName.getLast());
                passengers.add(passenger);
            }
            else if (passengerOrUsername instanceof String) {
                String frequentFlyerUsername = (String) passengerOrUsername;
                FrequentFlyer frequentFlyer = new FrequentFlyer(frequentFlyerUsername);
                passengers.add(frequentFlyer);
            }
        }
        org.springframework.ws.samples.airline.domain.Ticket domainTicket =
                airlineService.bookFlight(flightNumber, departureTime, passengers);
        return SchemaConversionUtils.toSchemaType(domainTicket);
    }

}
