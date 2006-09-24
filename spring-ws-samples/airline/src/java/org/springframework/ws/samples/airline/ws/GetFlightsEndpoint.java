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

import java.text.DateFormat;
import java.util.Iterator;
import java.util.List;

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
        if (logger.isDebugEnabled()) {
            DateFormat format = DateFormat.getDateInstance();
            String startDate =
                    (request.getStartOfPeriod() != null) ? format.format(request.getStartOfPeriod().getTime()) : null;
            String endDate =
                    (request.getEndOfPeriod() != null) ? format.format(request.getEndOfPeriod().getTime()) : null;
            logger.debug(
                    "GetFlightsRequest number=[" + request.getFlightNumber() + "," + startDate + "," + endDate + "]");
        }
        List flights = airlineService
                .getFlightsInPeriod(request.getFlightNumber(), request.getStartOfPeriod(), request.getEndOfPeriod());
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
        schemaFlight.setDepartureTime(domainFlight.getDepartureTime());
        schemaFlight.setDepartureAirport(convertToSchemaType(domainFlight.getDepartureAirport()));
        schemaFlight.setArrivalTime(domainFlight.getArrivalTime());
        schemaFlight.setArrivalAirport(convertToSchemaType(domainFlight.getArrivalAirport()));
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

}
