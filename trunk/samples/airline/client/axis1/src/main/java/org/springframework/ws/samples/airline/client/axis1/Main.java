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

package org.springframework.ws.samples.airline.client.axis1;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import javax.xml.rpc.ServiceException;

/**
 * Simple client that calls the <code>GetFlights</code> and <code>BookFlight</code> operations using JAX-RPC (Axis 1).
 *
 * @author Arjen Poutsma
 */
public class Main {

    public static void main(String[] args) throws ServiceException, RemoteException {
        AirlineServiceLocator service = new AirlineServiceLocator();
        if (args.length > 0) {
            service.setAirlineSoap11EndpointAddress(args[0]);
        }
        Airline airline = service.getAirlineSoap11();
        GetFlightsRequest request = new GetFlightsRequest();
        request.setFrom("AMS");
        request.setTo("VCE");
        Calendar departureCalendar = Calendar.getInstance();
        departureCalendar.set(Calendar.YEAR, 2006);
        departureCalendar.set(Calendar.MONTH, Calendar.JANUARY);
        departureCalendar.set(Calendar.DATE, 31);
        Date departureDate = departureCalendar.getTime();
        request.setDepartureDate(departureDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println("Requesting flights on " + dateFormat.format(departureDate));
        Flight[] flights = airline.getFlights(request).getFlight();
        System.out.println("Got " + flights.length + " results");
        if (flights.length > 0)
        {
            // Book the first flight using John Doe as a frequent flyer
            BookFlightRequest bookFlightRequest = new BookFlightRequest();
            bookFlightRequest.setFlightNumber(flights[0].getNumber());
            bookFlightRequest.setDepartureTime(flights[0].getDepartureTime());
            BookFlightRequestPassengers passengers = new BookFlightRequestPassengers();
            passengers.setUsername("john");
            bookFlightRequest.setPassengers(passengers);
            Ticket ticket = airline.bookFlight(bookFlightRequest);
            writeTicket(ticket);
        }
    }

    private static void writeTicket(Ticket ticket) {
        System.out.println("Ticket " + ticket.getId());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println("Ticket issue date:\t" + dateFormat.format(ticket.getIssueDate()));
        for (int i = 0; i < ticket.getPassengers().getPassenger().length; i++) {
            writeName(ticket.getPassengers().getPassenger()[i]);

        }
        writeFlight(ticket.getFlight());
    }

    private static void writeName(Name name) {
        System.out.println("Passenger Name:");
        System.out.println(name.getFirst() + " " + name.getLast());
        System.out.println("------------");
    }

    private static void writeFlight(Flight flight) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        System.out.println(dateFormat.format(flight.getDepartureTime().getTime()));
        System.out.println(flight.getNumber() + "\t" + flight.getServiceClass());
        System.out.println("------------");
        System.out.println("Depart:\t" + flight.getFrom().getCode() + "-" + flight.getFrom().getName() + "\t" + dateFormat.format(flight.getDepartureTime().getTime()));
        System.out.println("\t" + flight.getFrom().getCity());
        System.out.println("Arrive:\t" + flight.getTo().getCode() + "-" + flight.getTo().getName() + "\t" + dateFormat.format(flight.getArrivalTime().getTime()));
        System.out.println("\t" + flight.getTo().getCity());
    }


}
