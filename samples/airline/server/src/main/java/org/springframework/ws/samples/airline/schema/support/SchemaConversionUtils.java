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

package org.springframework.ws.samples.airline.schema.support;

import java.util.ArrayList;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.springframework.ws.samples.airline.domain.Passenger;
import org.springframework.ws.samples.airline.schema.Airport;
import org.springframework.ws.samples.airline.schema.Flight;
import org.springframework.ws.samples.airline.schema.Name;
import org.springframework.ws.samples.airline.schema.ServiceClass;
import org.springframework.ws.samples.airline.schema.Ticket;

/** @author Arjen Poutsma */
public abstract class SchemaConversionUtils {

    private SchemaConversionUtils() {
    }

    public static Flight toSchemaType(org.springframework.ws.samples.airline.domain.Flight domainFlight)
            throws DatatypeConfigurationException {
        Flight schemaFlight = new Flight();
        schemaFlight.setNumber(domainFlight.getNumber());
        schemaFlight.setDepartureTime(toXMLGregorianCalendar(domainFlight.getDepartureTime()));
        schemaFlight.setFrom(toSchemaType(domainFlight.getFrom()));
        schemaFlight.setArrivalTime(toXMLGregorianCalendar(domainFlight.getArrivalTime()));
        schemaFlight.setTo(toSchemaType(domainFlight.getTo()));
        schemaFlight.setServiceClass(toSchemaType(domainFlight.getServiceClass()));
        return schemaFlight;
    }

    public static List<Flight> toSchemaType(List<org.springframework.ws.samples.airline.domain.Flight> domainFlights)
            throws DatatypeConfigurationException {
        List<Flight> schemaFlights = new ArrayList<Flight>(domainFlights.size());
        for (org.springframework.ws.samples.airline.domain.Flight domainFlight : domainFlights) {
            schemaFlights.add(toSchemaType(domainFlight));
        }
        return schemaFlights;
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(DateTime dateTime) throws DatatypeConfigurationException {
        DatatypeFactory factory = DatatypeFactory.newInstance();
        return factory.newXMLGregorianCalendar(dateTime.toGregorianCalendar());
    }

    public static DateTime toDateTime(XMLGregorianCalendar calendar) {
        int timeZoneMinutes = calendar.getTimezone();
        DateTimeZone timeZone = DateTimeZone.forOffsetHoursMinutes(timeZoneMinutes / 60, timeZoneMinutes % 60);
        return new DateTime(calendar.getYear(), calendar.getMonth(), calendar.getDay(), calendar.getHour(),
                calendar.getMinute(), calendar.getSecond(), calendar.getMillisecond(), timeZone);
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(LocalDate localDate)
            throws DatatypeConfigurationException {
        DatatypeFactory factory = DatatypeFactory.newInstance();
        return factory.newXMLGregorianCalendarDate(localDate.getYear(), localDate.getMonthOfYear(),
                localDate.getDayOfMonth(), DatatypeConstants.FIELD_UNDEFINED);
    }

    public static LocalDate toLocalDate(XMLGregorianCalendar calendar) {
        return new LocalDate(calendar.getYear(), calendar.getMonth(), calendar.getDay());
    }

    public static Airport toSchemaType(org.springframework.ws.samples.airline.domain.Airport domainAirport) {
        if (domainAirport == null) {
            return null;
        }
        Airport schemaAirport = new Airport();
        schemaAirport.setCode(domainAirport.getCode());
        schemaAirport.setName(domainAirport.getName());
        schemaAirport.setCity(domainAirport.getCity());
        return schemaAirport;
    }

    public static ServiceClass toSchemaType(org.springframework.ws.samples.airline.domain.ServiceClass domainServiceClass) {
        switch (domainServiceClass) {
            case BUSINESS:
                return ServiceClass.BUSINESS;
            case ECONOMY:
                return ServiceClass.ECONOMY;
            case FIRST:
                return ServiceClass.FIRST;
            default:
                throw new IllegalArgumentException("Invalid domain service class: [" + domainServiceClass + "]");
        }
    }

    public static org.springframework.ws.samples.airline.domain.ServiceClass toDomainType(ServiceClass schemaServiceClass) {
        if (schemaServiceClass == null) {
            return null;
        }
        switch (schemaServiceClass) {
            case BUSINESS:
                return org.springframework.ws.samples.airline.domain.ServiceClass.BUSINESS;
            case ECONOMY:
                return org.springframework.ws.samples.airline.domain.ServiceClass.ECONOMY;
            case FIRST:
                return org.springframework.ws.samples.airline.domain.ServiceClass.FIRST;
            default:
                throw new IllegalArgumentException("Invalid schema service class: [" + schemaServiceClass + "]");
        }
    }

    public static Name toSchemaType(org.springframework.ws.samples.airline.domain.Passenger passenger) {
        Name name = new Name();
        name.setFirst(passenger.getFirstName());
        name.setLast(passenger.getLastName());
        return name;
    }

    public static Ticket toSchemaType(org.springframework.ws.samples.airline.domain.Ticket domainTicket)
            throws DatatypeConfigurationException {
        Ticket schemaTicket = new Ticket();
        schemaTicket.setId(domainTicket.getId());
        schemaTicket.setFlight(toSchemaType(domainTicket.getFlight()));
        schemaTicket.setIssueDate(toXMLGregorianCalendar(domainTicket.getIssueDate()));
        if (!domainTicket.getPassengers().isEmpty()) {
            schemaTicket.setPassengers(new Ticket.Passengers());
        }
        for (Passenger passenger : domainTicket.getPassengers()) {
            schemaTicket.getPassengers().getPassenger().add(toSchemaType(passenger));
        }
        return schemaTicket;
    }
}
