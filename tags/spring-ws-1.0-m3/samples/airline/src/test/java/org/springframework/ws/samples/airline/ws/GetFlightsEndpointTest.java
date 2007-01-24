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
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.oxm.jaxb.Jaxb1Marshaller;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.schema.GetFlightsRequest;
import org.springframework.ws.samples.airline.schema.GetFlightsResponse;
import org.springframework.ws.samples.airline.schema.ServiceClass;
import org.springframework.ws.samples.airline.schema.impl.GetFlightsRequestImpl;
import org.springframework.ws.samples.airline.service.AirlineService;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public class GetFlightsEndpointTest extends TestCase {

    private GetFlightsEndpoint endpoint;

    private MockControl serviceControl;

    private AirlineService serviceMock;

    protected void setUp() throws Exception {
        endpoint = new GetFlightsEndpoint();
        serviceControl = MockControl.createControl(AirlineService.class);
        serviceMock = (AirlineService) serviceControl.getMock();
        endpoint.setAirlineService(serviceMock);
    }

    public void testInvoke() throws Exception {
        String fromAirportCode = "ABC";
        String toAirportCode = "DEF";
        LocalDate departureDate = new LocalDate();
        GetFlightsRequest request = new GetFlightsRequestImpl();
        request.setFrom(fromAirportCode);
        request.setTo(toAirportCode);
        request.setDepartureDate(departureDate.toDateTimeAtMidnight().toGregorianCalendar());
        request.setServiceClass(ServiceClass.FIRST);
        List flights = new ArrayList();
        Flight flight = new Flight();
        flight.setNumber("1");
        flight.setDepartureTime(new DateTime());
        flight.setArrivalTime(new DateTime());
        flights.add(flight);
        serviceControl.expectAndReturn(serviceMock.getFlights(fromAirportCode, toAirportCode, departureDate,
                org.springframework.ws.samples.airline.domain.ServiceClass.FIRST), flights);
        serviceControl.replay();
        GetFlightsResponse response = (GetFlightsResponse) endpoint.invokeInternal(request);
        serviceControl.verify();
        assertNotNull("Response is null", response);
        assertEquals("Invalid amount of flights in response", 1, response.getFlight().size());
        org.springframework.ws.samples.airline.schema.Flight responseFlight =
                (org.springframework.ws.samples.airline.schema.Flight) response.getFlight().get(0);
        assertEquals("Invalid flight number on flight", "1", responseFlight.getNumber());
    }

    public void testMarshalling() throws Exception {
        Jaxb1Marshaller marshaller = new Jaxb1Marshaller();
        marshaller.setContextPath("org.springframework.ws.samples.airline.schema");
        marshaller.setValidating(true);
        marshaller.afterPropertiesSet();
        endpoint.setMarshaller(marshaller);
        endpoint.setUnmarshaller(marshaller);

        MockControl contextControl = MockControl.createControl(MessageContext.class);
        MessageContext contextMock = (MessageContext) contextControl.getMock();
        MockControl messageControl = MockControl.createControl(WebServiceMessage.class);
        WebServiceMessage requestMock = (WebServiceMessage) messageControl.getMock();
        WebServiceMessage responseMock = (WebServiceMessage) messageControl.getMock();

        contextControl.expectAndReturn(contextMock.getRequest(), requestMock);
        StringSource source = new StringSource(
                "<GetFlightsRequest xmlns=\"http://www.springframework.org/spring-ws/samples/airline/schemas\"><from>AMS</from><to>VCE</to><departureDate>2006-01-31Z</departureDate></GetFlightsRequest>");
        messageControl.expectAndReturn(requestMock.getPayloadSource(), source);
        String fromAirportCode = "AMS";
        String toAirportCode = "VCE";
        LocalDate departureDate = new LocalDate(2006, 1, 31);
        serviceControl.expectAndReturn(serviceMock.getFlights(fromAirportCode, toAirportCode, departureDate, null),
                Collections.EMPTY_LIST);
        contextControl.expectAndReturn(contextMock.getResponse(), responseMock);
        StringResult result = new StringResult();
        messageControl.expectAndReturn(responseMock.getPayloadResult(), result);

        contextControl.replay();
        messageControl.replay();
        serviceControl.replay();

        endpoint.invoke(contextMock);

        contextControl.verify();
        messageControl.verify();
        serviceControl.verify();
    }

}
