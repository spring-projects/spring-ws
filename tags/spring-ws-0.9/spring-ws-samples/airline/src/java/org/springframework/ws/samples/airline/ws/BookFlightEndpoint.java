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

import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.springframework.ws.endpoint.AbstractDomPayloadEndpoint;
import org.springframework.ws.samples.airline.domain.Airport;
import org.springframework.ws.samples.airline.domain.Customer;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.ServiceClass;
import org.springframework.ws.samples.airline.domain.Ticket;
import org.springframework.ws.samples.airline.service.AirlineService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BookFlightEndpoint extends AbstractDomPayloadEndpoint {

    private AirlineService airlineService;

    private XPathExpression flightNumberXPathExpression;

    private XPathExpression customerIdXPathExpression;

    private static final String PREFIX = "tns";

    private static final String NAMESPACE = "http://www.springframework.org/spring-ws/samples/airline";

    private DatatypeFactory datatypeFactory;

    private static final String FLIGHT_NUMBER_EXPRESSION =
            "/" + PREFIX + ":BookFlightRequest/" + PREFIX + ":flightNumber/text()";

    private static final String CUSTOMER_ID_EXPRESSION =
            "/" + PREFIX + ":BookFlightRequest/" + PREFIX + ":customerId/text()";

    public void setAirlineService(AirlineService airlineService) {
        this.airlineService = airlineService;
    }

    protected Element invokeInternal(Element requestElement, Document document) throws Exception {
        String flightNumber = flightNumberXPathExpression.evaluate(requestElement);
        long customerId =
                ((Double) customerIdXPathExpression.evaluate(requestElement, XPathConstants.NUMBER)).longValue();

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "BookFlight request for flight number [" + flightNumber + "] customer id [" + customerId + "]");
        }
        Ticket ticket = airlineService.bookFlight(flightNumber, customerId);

        return createResponse(ticket, document);
    }

    private Element createResponse(Ticket ticket, Document document) {
        Element responseElement = document.createElementNS(NAMESPACE, "BookFlightResponse");
        document.appendChild(responseElement);
        addIssueDateElement(document, responseElement, ticket.getIssueDate());
        addCustomerElement(document, responseElement, ticket.getCustomer());
        addFlightElement(document, responseElement, ticket.getFlight());
        return responseElement;
    }

    private void addIssueDateElement(Document document, Element responseElement, Calendar issueDate) {
        Element issueDateElement = document.createElementNS(NAMESPACE, "issueDate");
        responseElement.appendChild(issueDateElement);
        XMLGregorianCalendar issueDateCalendar = datatypeFactory.newXMLGregorianCalendarDate(
                issueDate.get(Calendar.YEAR), issueDate.get(Calendar.MONTH) + 1, issueDate.get(Calendar.DAY_OF_MONTH),
                DatatypeConstants.FIELD_UNDEFINED);
        issueDateElement.setTextContent(issueDateCalendar.toXMLFormat());
    }

    private void addCustomerElement(Document document, Element responseElement, Customer customer) {
        Element customerElement = document.createElementNS(NAMESPACE, "customer");
        responseElement.appendChild(customerElement);
        Element customerIdElement = document.createElementNS(NAMESPACE, "id");
        customerIdElement.setTextContent(String.valueOf(customer.getId()));
        customerElement.appendChild(customerIdElement);
        Element customerNameElement = document.createElementNS(NAMESPACE, "name");
        customerElement.appendChild(customerNameElement);
        Element customerFirstNameElement = document.createElementNS(NAMESPACE, "first");
        customerFirstNameElement.setTextContent(customer.getFirstName());
        customerNameElement.appendChild(customerFirstNameElement);
        Element customerLastNameElement = document.createElementNS(NAMESPACE, "last");
        customerLastNameElement.setTextContent(customer.getLastName());
        customerNameElement.appendChild(customerLastNameElement);
    }

    private void addFlightElement(Document document, Element responseElement, Flight flight) {
        Element flightElement = document.createElementNS(NAMESPACE, "flight");
        responseElement.appendChild(flightElement);
        Element flightNumberElement = document.createElementNS(NAMESPACE, "number");
        flightElement.appendChild(flightNumberElement);
        flightNumberElement.setTextContent(flight.getNumber());
        Element departureTimeElement = document.createElementNS(NAMESPACE, "departureTime");
        flightElement.appendChild(departureTimeElement);
        Element departureAirportElement = document.createElementNS(NAMESPACE, "departureAirport");
        flightElement.appendChild(departureAirportElement);
        addAirportElement(document, departureAirportElement, flight.getDepartureAirport());
        XMLGregorianCalendar departureCalendar =
                datatypeFactory.newXMLGregorianCalendar((GregorianCalendar) flight.getDepartureTime());
        departureTimeElement.setTextContent(departureCalendar.toXMLFormat());
        Element arrivalTimeElement = document.createElementNS(NAMESPACE, "arrivalTime");
        flightElement.appendChild(arrivalTimeElement);
        XMLGregorianCalendar arrivalCalendar =
                datatypeFactory.newXMLGregorianCalendar((GregorianCalendar) flight.getArrivalTime());
        arrivalTimeElement.setTextContent(arrivalCalendar.toXMLFormat());
        Element arrivalAirportElement = document.createElementNS(NAMESPACE, "arrivalAirport");
        flightElement.appendChild(arrivalAirportElement);
        addAirportElement(document, arrivalAirportElement, flight.getArrivalAirport());
        addServiceClassElement(document, flightElement, flight.getServiceClass());
    }

    private void addAirportElement(Document document, Element airportElement, Airport airport) {
        Element codeElement = document.createElementNS(NAMESPACE, "code");
        airportElement.appendChild(codeElement);
        codeElement.setTextContent(airport.getCode());
        Element nameElement = document.createElementNS(NAMESPACE, "name");
        airportElement.appendChild(nameElement);
        nameElement.setTextContent(airport.getName());
        Element cityElement = document.createElementNS(NAMESPACE, "city");
        airportElement.appendChild(cityElement);
        cityElement.setTextContent(airport.getCity());
    }

    private void addServiceClassElement(Document document, Element flightElement, ServiceClass serviceClass) {
        Element serviceClassElement = document.createElementNS(NAMESPACE, "serviceClass");
        flightElement.appendChild(serviceClassElement);
        if (ServiceClass.BUSINESS.equals(serviceClass)) {
            serviceClassElement.setTextContent("business");
        }
        else if (ServiceClass.ECONOMY.equals(serviceClass)) {
            serviceClassElement.setTextContent("economy");
        }
        else if (ServiceClass.FIRST.equals(serviceClass)) {
            serviceClassElement.setTextContent("first");
        }
    }

    public void onAfterPropertiesSet() throws Exception {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new MyNamespaceHelper());
        flightNumberXPathExpression = xpath.compile(FLIGHT_NUMBER_EXPRESSION);
        customerIdXPathExpression = xpath.compile(CUSTOMER_ID_EXPRESSION);
        datatypeFactory = DatatypeFactory.newInstance();
    }

    private static class MyNamespaceHelper implements NamespaceContext {

        public String getNamespaceURI(String prefix) {
            if (PREFIX.equals(prefix)) {
                return NAMESPACE;
            }
            else {
                return XMLConstants.NULL_NS_URI;
            }
        }

        public String getPrefix(String namespaceURI) {
            return NAMESPACE.equals(namespaceURI) ? PREFIX : null;
        }

        public Iterator getPrefixes(String namespaceURI) {
            if (NAMESPACE.equals(namespaceURI)) {
                return Collections.singletonList(PREFIX).iterator();
            }
            else {
                return Collections.EMPTY_LIST.iterator();
            }
        }
    }
}
