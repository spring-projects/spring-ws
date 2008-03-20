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

package org.springframework.ws.samples.airline.client.jaxws;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPFaultException;

/**
 * Simple client that calls the <code>GetFlights</code> and <code>BookFlight</code> operations using JAX-WS.
 *
 * @author Arjen Poutsma
 */
public class Main {

    public static void main(String[] args) throws MalformedURLException, DatatypeConfigurationException {
        try {
            AirlineService service;
            if (args.length == 0) {
                service = new AirlineService();
            }
            else {
                QName serviceName = new QName("http://www.springframework.org/spring-ws/samples/airline/definitions",
                        "AirlineService");
                service = new AirlineService(new URL(args[0]), serviceName);
            }
            Airline airline = service.getAirlineSoap11();
            GetFlightsRequest request = new GetFlightsRequest();
            request.setFrom("AMS");
            request.setTo("VCE");
            XMLGregorianCalendar departureDate =
                    DatatypeFactory.newInstance().newXMLGregorianCalendarDate(2006, 1, 31,
                    DatatypeConstants.FIELD_UNDEFINED);
            request.setDepartureDate(departureDate);
            System.out.format("Requesting flights on %1tD%n", departureDate.toGregorianCalendar());
            GetFlightsResponse response = airline.getFlights(request);
            System.out.format("Got %1d results%n", response.getFlight().size());
            if (!response.getFlight().isEmpty())
            // Book the first flight using John Doe as a frequent flyer
            {
                Flight flight = response.getFlight().get(0);
                BookFlightRequest bookFlightRequest = new BookFlightRequest();
                bookFlightRequest.setFlightNumber(flight.getNumber());
                bookFlightRequest.setDepartureTime(flight.getDepartureTime());
                BookFlightRequest.Passengers passengers = new BookFlightRequest.Passengers();
                passengers.getPassengerOrUsername().add("john");
                bookFlightRequest.setPassengers(passengers);
                Ticket ticket = airline.bookFlight(bookFlightRequest);
                writeTicket(ticket);
            }
        }
        catch (SOAPFaultException ex) {
            System.out.format("SOAP Fault Code    %1s%n", ex.getFault().getFaultCodeAsQName());
            System.out.format("SOAP Fault String: %1s%n", ex.getFault().getFaultString());

        }
    }

    private static void writeTicket(Ticket ticket) {
        System.out.format("Ticket %1d%n", ticket.getId());
        System.out.format("Ticket issue date:\t%1tD%n", ticket.getIssueDate().toGregorianCalendar());
        for (Name passenger : ticket.getPassengers().getPassenger()) {
            writeName(passenger);

        }
        writeFlight(ticket.flight);
    }

    private static void writeName(Name name) {
        System.out.format("Passenger Name:%n");
        System.out.format("%1s %2s%n", name.getFirst(), name.getLast());
        System.out.format("------------%n");
    }

    private static void writeFlight(Flight flight) {
        System.out.format("%1tD%n", flight.getDepartureTime().toGregorianCalendar());
        System.out.format("%1s\t%2s%n", flight.getNumber(), flight.getServiceClass());
        System.out.format("------------%n");
        System.out.format("Depart:\t%1s-%2s\t%tR%n", flight.getFrom().getCode(), flight.getFrom().getName(), flight.getDepartureTime().toGregorianCalendar());
        System.out.format("\t%1s%n", flight.getFrom().getCity());
        System.out.format("Arrive:\t%1s-%2s\t%tR%n", flight.getTo().getCode(), flight.getTo().getName(), flight.getArrivalTime().toGregorianCalendar());
        System.out.format("\t%1s%n", flight.getTo().getCity());
    }

}
