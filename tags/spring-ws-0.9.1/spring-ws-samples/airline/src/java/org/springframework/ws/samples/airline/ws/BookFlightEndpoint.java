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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ws.endpoint.AbstractJDomPayloadEndpoint;
import org.springframework.ws.samples.airline.domain.Airport;
import org.springframework.ws.samples.airline.domain.Customer;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.ServiceClass;
import org.springframework.ws.samples.airline.domain.Ticket;
import org.springframework.ws.samples.airline.service.AirlineService;

public class BookFlightEndpoint extends AbstractJDomPayloadEndpoint implements InitializingBean {

    private AirlineService airlineService;

    private XPath flightNumberXPath;

    private XPath customerIdXPath;

    private static final String PREFIX = "tns";

    private static final String NAMESPACE = "http://www.springframework.org/spring-ws/samples/airline";

    private static final String FLIGHT_NUMBER_EXPRESSION =
            "/" + PREFIX + ":BookFlightRequest/" + PREFIX + ":flightNumber/text()";

    private static final String CUSTOMER_ID_EXPRESSION =
            "/" + PREFIX + ":BookFlightRequest/" + PREFIX + ":customerId/text()";

    private Namespace namespace;

    public void setAirlineService(AirlineService airlineService) {
        this.airlineService = airlineService;
    }

    protected Element invokeInternal(Element requestElement) throws Exception {
        String flightNumber = flightNumberXPath.valueOf(requestElement);
        long customerId = Long.parseLong(customerIdXPath.valueOf(requestElement));
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "BookFlight request for flight number [" + flightNumber + "] customer id [" + customerId + "]");
        }
        Ticket ticket = airlineService.bookFlight(flightNumber, customerId);
        return createResponse(ticket);
    }

    private Element createResponse(Ticket ticket) {
        Element responseElement = new Element("BookFlightResponse", namespace);
        responseElement.addContent(createIssueDateElement(ticket.getIssueDate()));
        responseElement.addContent(createCustomerElement(ticket.getCustomer()));
        responseElement.addContent(createFlightElement(ticket.getFlight()));
        return responseElement;
    }

    protected Element createIssueDateElement(Calendar issueDate) {
        Element issueDateElement = new Element("issueDate", namespace);
        issueDateElement.setText(formatIsoDate(issueDate.getTime()));
        return issueDateElement;
    }

    protected Element createCustomerElement(Customer customer) {
        Element customerElement = new Element("customer", namespace);
        customerElement.addContent(new Element("id", namespace).setText(String.valueOf(customer.getId())));
        Element customerNameElement = new Element("name", namespace);
        customerElement.addContent(customerNameElement);
        customerNameElement.addContent(new Element("first", namespace).setText(customer.getFirstName()));
        customerNameElement.addContent(new Element("last", namespace).setText(customer.getLastName()));
        return customerElement;
    }

    protected Element createFlightElement(Flight flight) {
        Element flightElement = new Element("flight", namespace);
        flightElement.addContent(new Element("number", namespace).setText(flight.getNumber()));
        String departureTime = formatIsoDateTime(flight.getDepartureTime().getTime());
        flightElement.addContent(new Element("departureTime", namespace).setText(departureTime));
        flightElement.addContent(createAirportElement("departureAirport", flight.getDepartureAirport()));
        String arrivalTime = formatIsoDateTime(flight.getArrivalTime().getTime());
        flightElement.addContent(new Element("arrivalTime", namespace).setText(arrivalTime));
        flightElement.addContent(createAirportElement("arrivalAirport", flight.getArrivalAirport()));
        flightElement.addContent(createServiceClassElement(flight.getServiceClass()));
        return flightElement;
    }

    private String formatIsoDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private String formatIsoDateTime(Date date) {
        String result = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date);
        //convert YYYYMMDDTHH:mm:ss+HH00 into YYYYMMDDTHH:mm:ss+HH:00
        return result.substring(0, result.length() - 2) + ":" + result.substring(result.length() - 2);
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
        namespace = Namespace.getNamespace(PREFIX, NAMESPACE);
        flightNumberXPath = XPath.newInstance(FLIGHT_NUMBER_EXPRESSION);
        flightNumberXPath.addNamespace(namespace);
        customerIdXPath = XPath.newInstance(CUSTOMER_ID_EXPRESSION);
        customerIdXPath.addNamespace(namespace);
    }


}
