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

package org.springframework.ws.samples.airline.client.sws;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.springWs.samples.airline.schemas.BookFlightRequestDocument;
import org.springframework.springWs.samples.airline.schemas.BookFlightResponseDocument;
import org.springframework.springWs.samples.airline.schemas.Flight;
import org.springframework.springWs.samples.airline.schemas.GetFlightsRequestDocument;
import org.springframework.springWs.samples.airline.schemas.GetFlightsResponseDocument;
import org.springframework.springWs.samples.airline.schemas.Name;
import org.springframework.springWs.samples.airline.schemas.Ticket;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

public class AirlineClient extends WebServiceGatewaySupport {

    public void getFlights() {
        GetFlightsRequestDocument getFlightsRequestDocument = GetFlightsRequestDocument.Factory.newInstance();
        GetFlightsRequestDocument.GetFlightsRequest getFlightsRequest =
                getFlightsRequestDocument.addNewGetFlightsRequest();
        getFlightsRequest.setFrom("AMS");
        getFlightsRequest.setTo("VCE");
        Calendar departureDate = Calendar.getInstance();
        departureDate.clear();
        departureDate.set(2006, Calendar.JANUARY, 31);
        getFlightsRequest.setDepartureDate(departureDate);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println("Requesting flights on " + dateFormat.format(departureDate.getTime()));
        GetFlightsResponseDocument getFlightsResponseDocument =
                (GetFlightsResponseDocument) getWebServiceTemplate().marshalSendAndReceive(getFlightsRequestDocument);
        GetFlightsResponseDocument.GetFlightsResponse response = getFlightsResponseDocument.getGetFlightsResponse();
        System.out.println("Got " + response.sizeOfFlightArray() + " results");
        if (response.sizeOfFlightArray() > 0) {
            // Book the first flight using John Doe as a frequent flyer
            BookFlightRequestDocument bookFlightRequestDocument = BookFlightRequestDocument.Factory.newInstance();
            BookFlightRequestDocument.BookFlightRequest bookFlightRequest =
                    bookFlightRequestDocument.addNewBookFlightRequest();
            bookFlightRequest.setFlightNumber(response.getFlightArray(0).getNumber());
            bookFlightRequest.setDepartureTime(response.getFlightArray(0).getDepartureTime());
            BookFlightRequestDocument.BookFlightRequest.Passengers passengers = bookFlightRequest.addNewPassengers();
            passengers.addUsername("john");

            BookFlightResponseDocument bookFlightResponseDocument = (BookFlightResponseDocument) getWebServiceTemplate()
                    .marshalSendAndReceive(bookFlightRequestDocument);
            Ticket ticket = bookFlightResponseDocument.getBookFlightResponse();
            writeTicket(ticket);
        }
    }

    private static void writeTicket(Ticket ticket) {
        System.out.println("Ticket " + ticket.getId());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println("Ticket issue date:\t" + dateFormat.format(ticket.getIssueDate().getTime()));
        for (int i = 0; i < ticket.getPassengers().sizeOfPassengerArray(); i++) {
            writeName(ticket.getPassengers().getPassengerArray(i));

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
        System.out.println("Depart:\t" + flight.getFrom().getCode() + "-" + flight.getFrom().getName() + "\t" +
                dateFormat.format(flight.getDepartureTime().getTime()));
        System.out.println("\t" + flight.getFrom().getCity());
        System.out.println("Arrive:\t" + flight.getTo().getCode() + "-" + flight.getTo().getName() + "\t" +
                dateFormat.format(flight.getArrivalTime().getTime()));
        System.out.println("\t" + flight.getTo().getCity());
    }

    public static void main(String[] args) {
        ApplicationContext applicationContext =
                new ClassPathXmlApplicationContext("applicationContext.xml", AirlineClient.class);
        AirlineClient airlineClient = (AirlineClient) applicationContext.getBean("airlineClient", AirlineClient.class);
        airlineClient.getFlights();
    }
}
