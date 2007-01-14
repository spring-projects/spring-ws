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
package org.springframework.ws.samples.airline.ws;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.YearMonthDay;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ws.samples.airline.domain.Airport;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.FrequentFlyer;
import org.springframework.ws.samples.airline.domain.Passenger;
import org.springframework.ws.samples.airline.domain.ServiceClass;
import org.springframework.ws.samples.airline.domain.Ticket;
import org.springframework.ws.samples.airline.service.AirlineService;
import org.springframework.ws.server.endpoint.AbstractJDomPayloadEndpoint;

public class BookFlightEndpoint extends AbstractJDomPayloadEndpoint implements InitializingBean {

    private AirlineService airlineService;

    private XPath flightNumberXPath;

    private XPath departureTimeXPath;

    private Namespace namespace;

    private DateTimeFormatter dateFormatter;

    private DateTimeFormatter dateTimeFormatter;

    private DateTimeFormatter parser;

    private XPath passengersXPath;

    public void setAirlineService(AirlineService airlineService) {
        this.airlineService = airlineService;
    }

    protected Element invokeInternal(Element requestElement) throws Exception {
        String flightNumber = flightNumberXPath.valueOf(requestElement);
        String departureTimeString = departureTimeXPath.valueOf(requestElement);
        DateTime departureTime = parser.parseDateTime(departureTimeString);
        if (logger.isDebugEnabled()) {
            logger.debug("BookFlight request for flight number [" + flightNumber + "] at [" + departureTime + "]");
        }
        List passengerElements = passengersXPath.selectNodes(requestElement);
        List passengers = new ArrayList();
        for (Iterator iterator = passengerElements.iterator(); iterator.hasNext();) {
            Element passengerElement = (Element) iterator.next();
            if ("passenger".equals(passengerElement.getName()) && namespace.equals(passengerElement.getNamespace())) {
                Passenger passenger = new Passenger(passengerElement.getChildTextNormalize("first", namespace),
                        passengerElement.getChildTextNormalize("last", namespace));
                passengers.add(passenger);
            }
            else
            if ("username".equals(passengerElement.getName()) && namespace.equals(passengerElement.getNamespace())) {
                FrequentFlyer frequentFlyer = new FrequentFlyer(passengerElement.getTextNormalize());
                passengers.add(frequentFlyer);
            }
        }
        Ticket ticket = airlineService.bookFlight(flightNumber, departureTime, passengers);
        return createResponse(ticket);
    }

    private Element createResponse(Ticket ticket) {
        Element responseElement = new Element("BookFlightResponse", namespace);
        responseElement.addContent(new Element("id", namespace).setText(ticket.getId().toString()));
        responseElement.addContent(createIssueDateElement(ticket.getIssueDate()));
        responseElement.addContent(createPassengersElement(ticket.getPassengers()));
        responseElement.addContent(createFlightElement(ticket.getFlight()));
        return responseElement;
    }

    protected Element createIssueDateElement(YearMonthDay issueDate) {
        Element issueDateElement = new Element("issueDate", namespace);
        issueDateElement.setText(dateFormatter.print(issueDate));
        return issueDateElement;
    }

    protected Element createPassengersElement(Set passengers) {
        Element passengersElement = new Element("passengers", namespace);
        for (Iterator iterator = passengers.iterator(); iterator.hasNext();) {
            Passenger passenger = (Passenger) iterator.next();
            Element passengerElement = new Element("passenger", namespace);
            passengersElement.addContent(passengerElement);
            passengerElement.addContent(new Element("first", namespace).setText(passenger.getFirstName()));
            passengerElement.addContent(new Element("last", namespace).setText(passenger.getLastName()));
        }
        return passengersElement;
    }

    protected Element createFlightElement(Flight flight) {
        Element flightElement = new Element("flight", namespace);
        flightElement.addContent(new Element("number", namespace).setText(flight.getNumber()));
        flightElement.addContent(
                new Element("departureTime", namespace).setText(dateTimeFormatter.print(flight.getDepartureTime())));
        flightElement.addContent(createAirportElement("from", flight.getFrom()));
        flightElement
                .addContent(new Element("arrivalTime", namespace).setText(
                        dateTimeFormatter.print(flight.getArrivalTime())));
        flightElement.addContent(createAirportElement("to", flight.getTo()));
        flightElement.addContent(createServiceClassElement(flight.getServiceClass()));
        return flightElement;
    }

    protected Element createAirportElement(String localName, Airport airport) {
        Element airportElement = new Element(localName, namespace);
        airportElement.addContent(new Element("code", namespace).setText(airport.getCode()));
        airportElement.addContent(new Element("name", namespace).setText(airport.getName()));
        airportElement.addContent(new Element("city", namespace).setText(airport.getCity()));
        return airportElement;
    }

    protected Element createServiceClassElement(ServiceClass serviceClass) {
        Element serviceClassElement = new Element("serviceClass", namespace);
        if (ServiceClass.BUSINESS.equals(serviceClass)) {
            serviceClassElement.setText("business");
        }
        else if (ServiceClass.ECONOMY.equals(serviceClass)) {
            serviceClassElement.setText("economy");
        }
        else if (ServiceClass.FIRST.equals(serviceClass)) {
            serviceClassElement.setText("first");
        }
        return serviceClassElement;
    }

    public void afterPropertiesSet() throws Exception {
        namespace = Namespace.getNamespace("tns", "http://www.springframework.org/spring-ws/samples/airline/schemas");
        flightNumberXPath = XPath.newInstance("/tns:BookFlightRequest/tns:flightNumber/text()");
        flightNumberXPath.addNamespace(namespace);
        departureTimeXPath = XPath.newInstance("/tns:BookFlightRequest/tns:departureTime/text()");
        departureTimeXPath.addNamespace(namespace);
        passengersXPath = XPath.newInstance("/tns:BookFlightRequest/tns:passengers/*");
        passengersXPath.addNamespace(namespace);
        parser = ISODateTimeFormat.dateTimeParser().withZone(DateTimeZone.UTC);
        dateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis();
        dateFormatter = ISODateTimeFormat.date();
    }


}
