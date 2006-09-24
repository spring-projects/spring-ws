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
import java.io.StringWriter;
import java.util.Calendar;
import java.util.TimeZone;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.easymock.MockControl;
import org.springframework.ws.samples.airline.domain.Airport;
import org.springframework.ws.samples.airline.domain.Customer;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.ServiceClass;
import org.springframework.ws.samples.airline.domain.Ticket;
import org.springframework.ws.samples.airline.service.AirlineService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

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
                    "<flight><number>EF1234</number><departureTime>2006-01-01T00:00:00.000Z</departureTime>" +
                    "<departureAirport><code>ABC</code><name>Airport</name><city>City</city></departureAirport>" +
                    "<arrivalTime>2006-01-01T00:00:00.000Z</arrivalTime><arrivalAirport><code>ABC</code>" +
                    "<name>Airport</name><city>City</city></arrivalAirport><serviceClass>economy</serviceClass>" +
                    "</flight></BookFlightResponse>";

    private Document responseDocument;

    private Calendar calendar;

    protected void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        endpoint = new BookFlightEndpoint();
        serviceControl = MockControl.createControl(AirlineService.class);
        serviceMock = (AirlineService) serviceControl.getMock();
        endpoint.setAirlineService(serviceMock);
        endpoint.afterPropertiesSet();
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document requestDocument = builder.parse(new InputSource(new StringReader(REQUEST)));
        requestElement = ((Element) requestDocument.getFirstChild());
        responseDocument = builder.newDocument();
        calendar = Calendar.getInstance();
        calendar.set(2006, Calendar.JANUARY, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));

    }

    public void testInvoke() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setIssueDate(calendar);
        Customer customer = new Customer();
        customer.setId(new Long(42L));
        customer.setFirstName("firstName");
        customer.setLastName("lastName");
        ticket.setCustomer(customer);
        Flight flight = new Flight();
        flight.setNumber("EF1234");
        flight.setArrivalTime(calendar);
        Airport airport = new Airport();
        airport.setCode("ABC");
        airport.setName("Airport");
        airport.setCity("City");
        flight.setArrivalAirport(airport);
        flight.setDepartureTime(calendar);
        flight.setDepartureAirport(airport);
        flight.setServiceClass(ServiceClass.ECONOMY);
        ticket.setFlight(flight);
        serviceControl.expectAndReturn(serviceMock.bookFlight("EF1234", 42L), ticket);
        serviceControl.replay();
        Element result = endpoint.invokeInternal(requestElement, responseDocument);
        assertNotNull("Invalid result", result);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(result), new StreamResult(writer));
        assertXMLEqual("Invalid response", EXPECTED_RESPONSE, writer.toString());
        serviceControl.verify();
    }
}