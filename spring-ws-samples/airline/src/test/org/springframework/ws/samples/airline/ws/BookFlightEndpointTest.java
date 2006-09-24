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

import java.io.StringReader;
import java.util.Calendar;
import java.util.TimeZone;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.easymock.MockControl;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.springframework.ws.samples.airline.domain.Airport;
import org.springframework.ws.samples.airline.domain.Customer;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.ServiceClass;
import org.springframework.ws.samples.airline.domain.Ticket;
import org.springframework.ws.samples.airline.service.AirlineService;

public class BookFlightEndpointTest extends XMLTestCase {

    private BookFlightEndpoint endpoint;

    private Element requestElement;

    private MockControl serviceControl;

    private AirlineService serviceMock;

    private static final String REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<tns:BookFlightRequest xmlns:tns=\"http://www.springframework.org/spring-ws/samples/airline\">" +
            "<tns:flightNumber>EF1234</tns:flightNumber>\n" + "    <tns:customerId>42</tns:customerId>" +
            "</tns:BookFlightRequest>";

    public static final String EXPECTED_RESPONSE =
            "<BookFlightResponse xmlns=\"http://www.springframework.org/spring-ws/samples/airline\">" +
                    "<issueDate>2006-01-01</issueDate>" +
                    "<customer><id>42</id><name><first>firstName</first><last>lastName</last></name></customer>" +
                    "<flight><number>EF1234</number><departureTime>2006-01-01T01:00:00+01:00</departureTime>" +
                    "<departureAirport><code>ABC</code><name>Airport</name><city>City</city></departureAirport>" +
                    "<arrivalTime>2006-01-01T01:00:00+01:00</arrivalTime><arrivalAirport><code>ABC</code>" +
                    "<name>Airport</name><city>City</city></arrivalAirport><serviceClass>economy</serviceClass>" +
                    "</flight></BookFlightResponse>";

    private Calendar calendar;

    private Customer customer;

    private Airport airport;

    private Flight flight;

    protected void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        endpoint = new BookFlightEndpoint();
        serviceControl = MockControl.createControl(AirlineService.class);
        serviceMock = (AirlineService) serviceControl.getMock();
        endpoint.setAirlineService(serviceMock);
        endpoint.afterPropertiesSet();
        SAXBuilder saxBuilder = new SAXBuilder();
        Document requestDocument = saxBuilder.build(new StringReader(REQUEST));
        requestElement = requestDocument.getRootElement();
        calendar = Calendar.getInstance();
        calendar.set(2006, Calendar.JANUARY, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));

        customer = new Customer();
        customer.setId(new Long(42L));
        customer.setFirstName("firstName");
        customer.setLastName("lastName");

        airport = new Airport();
        airport.setCode("ABC");
        airport.setName("Airport");
        airport.setCity("City");

        flight = new Flight();
        flight.setNumber("EF1234");
        flight.setArrivalTime(calendar);
        flight.setArrivalAirport(airport);
        flight.setDepartureTime(calendar);
        flight.setDepartureAirport(airport);
        flight.setServiceClass(ServiceClass.ECONOMY);
    }

    public void testInvoke() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setIssueDate(calendar);
        ticket.setCustomer(customer);
        ticket.setFlight(flight);
        serviceControl.expectAndReturn(serviceMock.bookFlight("EF1234", 42L), ticket);
        serviceControl.replay();
        Element result = endpoint.invokeInternal(requestElement);
        assertNotNull("Invalid result", result);
        XMLOutputter outputter = new XMLOutputter();
        String resultXml = outputter.outputString(result);
        assertXMLEqual("Invalid response", EXPECTED_RESPONSE, resultXml);
        serviceControl.verify();
    }

    public void testCreateIssueDateElement() throws Exception {
        Element result = endpoint.createIssueDateElement(calendar);
        assertNotNull("No element returned", result);
        XMLOutputter outputter = new XMLOutputter();
        String resultXml = outputter.outputString(result);
        assertXMLEqual("Invalid result",
                "<issueDate xmlns='http://www.springframework.org/spring-ws/samples/airline'>2006-01-01</issueDate>",
                resultXml);
    }

    public void testCreateCustomerElement() throws Exception {
        Element result = endpoint.createCustomerElement(customer);
        assertNotNull("No element returned", result);
        XMLOutputter outputter = new XMLOutputter();
        String resultXml = outputter.outputString(result);
        assertXMLEqual("Invalid result",
                "<customer xmlns='http://www.springframework.org/spring-ws/samples/airline'><id>42</id><name><first>firstName</first><last>lastName</last></name></customer>",
                resultXml);
    }

    public void testCreateAirportElement() throws Exception {
        Element result = endpoint.createAirportElement("airport", airport);
        assertNotNull("No element returned", result);
        XMLOutputter outputter = new XMLOutputter();
        String resultXml = outputter.outputString(result);
        assertXMLEqual("Invalid result",
                "<airport  xmlns='http://www.springframework.org/spring-ws/samples/airline'><code>ABC</code><name>Airport</name><city>City</city></airport>",
                resultXml);
    }

    public void testCreateServiceClassElementBusiness() throws Exception {
        Element result = endpoint.createServiceClassElement(ServiceClass.BUSINESS);
        assertNotNull("No element returned", result);
        XMLOutputter outputter = new XMLOutputter();
        String resultXml = outputter.outputString(result);
        assertXMLEqual("Invalid result",
                "<serviceClass xmlns='http://www.springframework.org/spring-ws/samples/airline'>business</serviceClass>",
                resultXml);
    }

    public void testCreateServiceClassElementEconomy() throws Exception {
        Element result = endpoint.createServiceClassElement(ServiceClass.ECONOMY);
        assertNotNull("No element returned", result);
        XMLOutputter outputter = new XMLOutputter();
        String resultXml = outputter.outputString(result);
        assertXMLEqual("Invalid result",
                "<serviceClass xmlns='http://www.springframework.org/spring-ws/samples/airline'>economy</serviceClass>",
                resultXml);
    }

    public void testCreateServiceClassElementFirst() throws Exception {
        Element result = endpoint.createServiceClassElement(ServiceClass.FIRST);
        assertNotNull("No element returned", result);
        XMLOutputter outputter = new XMLOutputter();
        String resultXml = outputter.outputString(result);
        assertXMLEqual("Invalid result",
                "<serviceClass xmlns='http://www.springframework.org/spring-ws/samples/airline'>first</serviceClass>",
                resultXml);
    }

    public void testCreateFlightElement() throws Exception {
        Element result = endpoint.createFlightElement(flight);
        assertNotNull("No element returned", result);
        XMLOutputter outputter = new XMLOutputter();
        String resultXml = outputter.outputString(result);
        assertXMLEqual("Invalid result", "<flight xmlns='http://www.springframework.org/spring-ws/samples/airline'>" +
                "<number>EF1234</number>" + "<departureTime>2006-01-01T01:00:00+01:00</departureTime>" +
                "<departureAirport><code>ABC</code><name>Airport</name><city>City</city></departureAirport>" +
                "<arrivalTime>2006-01-01T01:00:00+01:00</arrivalTime>" +
                "<arrivalAirport><code>ABC</code><name>Airport</name><city>City</city></arrivalAirport>" +
                "<serviceClass>economy</serviceClass>" + "</flight>", resultXml);
    }

}