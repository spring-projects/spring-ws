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

import java.util.Iterator;
import java.util.List;

import org.joda.time.YearMonthDay;
import org.joda.time.chrono.ISOChronology;

import org.springframework.ws.endpoint.AbstractMarshallingPayloadEndpoint;
import org.springframework.ws.samples.airline.schema.Airport;
import org.springframework.ws.samples.airline.schema.Flight;
import org.springframework.ws.samples.airline.schema.GetFlightsRequest;
import org.springframework.ws.samples.airline.schema.GetFlightsResponse;
import org.springframework.ws.samples.airline.schema.ServiceClass;
import org.springframework.ws.samples.airline.schema.impl.AirportImpl;
import org.springframework.ws.samples.airline.schema.impl.FlightImpl;
import org.springframework.ws.samples.airline.schema.impl.GetFlightsResponseImpl;
import org.springframework.ws.samples.airline.service.AirlineService;

/**
 * Endpoint that returns a list of flights with a given number, and that lie between a given start and end date. It uses
 * JAXB-based marshalling for both request and response objects. Because we use separate POJOs for our schema and our
 * domain objects, we need to convert the response domain objects to schema-based objects.
 *
 * @author Arjen Poutsma
 */
public class GetFlightsEndpoint extends AbstractMarshallingPayloadEndpoint {

    private AirlineService airlineService;

    public void setAirlineService(AirlineService airlineService) {
        this.airlineService = airlineService;
    }

    protected Object invokeInternal(Object requestObject) throws Exception {
        GetFlightsRequest request = (GetFlightsRequest) requestObject;
        YearMonthDay departureDate = new YearMonthDay(request.getDepartureDate(), ISOChronology.getInstance());
        if (logger.isDebugEnabled()) {
            logger.debug("Request for flights from '" + request.getFrom() + "' to '" + request.getTo() + "' on " +
                    departureDate);
        }
        List flights = airlineService.getFlights(request.getFrom(), request.getTo(), departureDate,
                convertToDomainType(request.getServiceClass()));
        if (logger.isDebugEnabled()) {
            logger.debug("Marshalling " + flights.size() + " flight results");
        }
        GetFlightsResponse response = new GetFlightsResponseImpl();
        for (Iterator iter = flights.iterator(); iter.hasNext();) {
            org.springframework.ws.samples.airline.domain.Flight domainFlight =
                    (org.springframework.ws.samples.airline.domain.Flight) iter.next();
            Flight schemaFlight = convertToSchemaType(domainFlight);
            response.getFlight().add(schemaFlight);
        }
        return response;
    }

    private Flight convertToSchemaType(org.springframework.ws.samples.airline.domain.Flight domainFlight) {
        Flight schemaFlight = new FlightImpl();
        schemaFlight.setNumber(domainFlight.getNumber());
        schemaFlight.setDepartureTime(domainFlight.getDepartureTime().toGregorianCalendar());
        schemaFlight.setFrom(convertToSchemaType(domainFlight.getFrom()));
        schemaFlight.setArrivalTime(domainFlight.getArrivalTime().toGregorianCalendar());
        schemaFlight.setTo(convertToSchemaType(domainFlight.getTo()));
        schemaFlight.setServiceClass(convertToSchemaType(domainFlight.getServiceClass()));
        return schemaFlight;
    }

    private Airport convertToSchemaType(org.springframework.ws.samples.airline.domain.Airport domainAirport) {
        if (domainAirport == null) {
            return null;
        }
        Airport schemaAirport = new AirportImpl();
        schemaAirport.setCode(domainAirport.getCode());
        schemaAirport.setName(domainAirport.getName());
        schemaAirport.setCity(domainAirport.getCity());
        return schemaAirport;
    }

    private ServiceClass convertToSchemaType(org.springframework.ws.samples.airline.domain.ServiceClass domainServiceClass) {
        if (domainServiceClass == null) {
            return null;
        }
        else if (domainServiceClass.equals(org.springframework.ws.samples.airline.domain.ServiceClass.BUSINESS)) {
            return ServiceClass.BUSINESS;
        }
        else if (domainServiceClass.equals(org.springframework.ws.samples.airline.domain.ServiceClass.ECONOMY)) {
            return ServiceClass.ECONOMY;
        }
        else if (domainServiceClass.equals(org.springframework.ws.samples.airline.domain.ServiceClass.FIRST)) {
            return ServiceClass.FIRST;
        }
        else {
            throw new IllegalArgumentException("Invalid domain service class: [" + domainServiceClass + "]");
        }
    }

    private org.springframework.ws.samples.airline.domain.ServiceClass convertToDomainType(ServiceClass schemaServiceClass) {
        if (schemaServiceClass == null) {
            return null;
        }
        else if (schemaServiceClass.equals(ServiceClass.BUSINESS)) {
            return org.springframework.ws.samples.airline.domain.ServiceClass.BUSINESS;
        }
        else if (schemaServiceClass.equals(ServiceClass.ECONOMY)) {
            return org.springframework.ws.samples.airline.domain.ServiceClass.ECONOMY;
        }
        else if (schemaServiceClass.equals(ServiceClass.FIRST)) {
            return org.springframework.ws.samples.airline.domain.ServiceClass.FIRST;
        }
        else {
            throw new IllegalArgumentException("Invalid schema service class: [" + schemaServiceClass + "]");
        }

    }

}
