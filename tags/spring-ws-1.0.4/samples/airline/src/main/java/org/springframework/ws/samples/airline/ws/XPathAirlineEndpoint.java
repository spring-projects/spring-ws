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

package org.springframework.ws.samples.airline.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.transform.Source;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.support.MarshallingSource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.FrequentFlyer;
import org.springframework.ws.samples.airline.domain.Passenger;
import org.springframework.ws.samples.airline.domain.ServiceClass;
import org.springframework.ws.samples.airline.domain.Ticket;
import org.springframework.ws.samples.airline.schema.GetFlightsResponse;
import org.springframework.ws.samples.airline.schema.ObjectFactory;
import org.springframework.ws.samples.airline.schema.support.SchemaConversionUtils;
import org.springframework.ws.samples.airline.service.AirlineService;
import org.springframework.ws.samples.airline.service.NoSeatAvailableException;
import org.springframework.ws.samples.airline.service.NoSuchFlightException;
import org.springframework.ws.samples.airline.service.NoSuchFrequentFlyerException;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.XPathParam;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Endpoint that handles the Airline Web Service request messages using XPath expressions, and creates response messages
 * using JAXB2.
 * <p/>
 * This endpoint contains exactly the same logic as the {@link MarshallingAirlineEndpoint}, and is only provided as an
 * example. Typically, you will not have two endpoints with the same logic.
 *
 * @author Arjen Poutsma
 */
@Endpoint
public class XPathAirlineEndpoint implements AirlineWebServiceConstants {

    private static final Log logger = LogFactory.getLog(XPathAirlineEndpoint.class);

    private final AirlineService airlineService;

    private ObjectFactory objectFactory = new ObjectFactory();

    private final Marshaller marshaller;

    public XPathAirlineEndpoint(AirlineService airlineService, Marshaller marshaller) {
        Assert.notNull(airlineService, "airlineService must not be null");
        Assert.notNull(marshaller, "'marshaller' must not be null");
        this.airlineService = airlineService;
        this.marshaller = marshaller;
    }

    /**
     * This endpoint method uses XPath to handle message with a <code>&lt;GetFlightsRequest&gt;</code> payload.
     *
     * @param from                the from airport
     * @param to                  the to airport
     * @param departureDateString the string representation of the departure date
     * @param serviceClassString  the string representation of the service class
     */
    @PayloadRoot(localPart = GET_FLIGHTS_REQUEST, namespace = NAMESPACE)
    public Source getFlights(@XPathParam("//tns:from")String from,
                             @XPathParam("//tns:to")String to,
                             @XPathParam("//tns:departureDate")String departureDateString,
                             @XPathParam("//tns:serviceClass")String serviceClassString)
            throws DatatypeConfigurationException {
        if (logger.isDebugEnabled()) {
            logger.debug("Received GetFlightsRequest '" + from + "' to '" + to + "' on " + departureDateString);
        }
        LocalDate departureDate = new LocalDate(departureDateString);
        ServiceClass serviceClass = null;
        if (StringUtils.hasLength(serviceClassString)) {
            serviceClass = ServiceClass.valueOf(serviceClassString.toUpperCase());
        }
        List<Flight> flights = airlineService.getFlights(from, to, departureDate, serviceClass);

        GetFlightsResponse response = objectFactory.createGetFlightsResponse();
        for (Flight domainFlight : flights) {
            response.getFlight().add(SchemaConversionUtils.toSchemaType(domainFlight));
        }
        return new MarshallingSource(marshaller, response);
    }

    /**
     * This endpoint method uses XPath to handle message with a <code>&lt;BookFlightRequest&gt;</code> payload.
     *
     * @param flightNumber        the flight number
     * @param departureTimeString the string representation of the departure time
     * @param passengerNodes      the passenger nodes
     * @param frequentFlyerNodes  the frequent flyer nodes
     */
    @PayloadRoot(localPart = BOOK_FLIGHT_REQUEST, namespace = NAMESPACE)
    public Source bookFlight(@XPathParam("//tns:flightNumber")String flightNumber,
                             @XPathParam("//tns:departureTime")String departureTimeString,
                             @XPathParam("//tns:passengers/tns:passenger")NodeList passengerNodes,
                             @XPathParam("//tns:passengers/tns:username")NodeList frequentFlyerNodes) throws
            NoSeatAvailableException, NoSuchFlightException, NoSuchFrequentFlyerException,
            DatatypeConfigurationException, JAXBException {
        if (logger.isDebugEnabled()) {
            logger.debug("Received BookingFlightRequest '" + flightNumber + "' on '" + departureTimeString + "' for " +
                    passengerNodes.getLength() + " passengers and " + frequentFlyerNodes.getLength() +
                    " frequent flyers");
        }
        DateTime departureTime = new DateTime(departureTimeString);
        List<Passenger> passengers = new ArrayList<Passenger>();
        parsePassengers(passengerNodes, passengers);
        parseFrequentFlyers(frequentFlyerNodes, passengers);
        Ticket domainTicket = airlineService.bookFlight(flightNumber, departureTime, passengers);

        JAXBElement<org.springframework.ws.samples.airline.schema.Ticket> response =
                objectFactory.createBookFlightResponse(SchemaConversionUtils.toSchemaType(domainTicket));
        return new MarshallingSource(marshaller, response);
    }

    private void parsePassengers(NodeList passengerNodes, List<Passenger> passengers) {
        for (int i = 0; i < passengerNodes.getLength(); i++) {
            if (passengerNodes.item(i).getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element passengerElement = (Element) passengerNodes.item(i);
            Element firstNameElement = (Element) passengerElement.getElementsByTagNameNS(NAMESPACE, "first").item(0);
            Element lastNameElement = (Element) passengerElement.getElementsByTagNameNS(NAMESPACE, "last").item(0);
            Passenger passenger = new Passenger(firstNameElement.getTextContent(), lastNameElement.getTextContent());
            passengers.add(passenger);
        }
    }

    private void parseFrequentFlyers(NodeList frequentFlyerNodes, List<Passenger> passengers) {
        for (int i = 0; i < frequentFlyerNodes.getLength(); i++) {
            if (frequentFlyerNodes.item(i).getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element frequentFlyerElement = (Element) frequentFlyerNodes.item(i);
            FrequentFlyer frequentFlyer = new FrequentFlyer(frequentFlyerElement.getTextContent());
            passengers.add(frequentFlyer);
        }
    }


}
