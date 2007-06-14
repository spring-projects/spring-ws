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

import java.util.Collections;
import javax.xml.transform.Source;

import junit.framework.TestCase;
import static org.easymock.EasyMock.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.springframework.oxm.Marshaller;
import org.springframework.ws.samples.airline.domain.Airport;
import org.springframework.ws.samples.airline.service.AirlineService;

public class XPathAirlineEndpointTest extends TestCase {

    private XPathAirlineEndpoint endpoint;

    private AirlineService airlineServiceMock;

    private Marshaller marshallerMock;

    protected void setUp() throws Exception {
        airlineServiceMock = createMock(AirlineService.class);
        marshallerMock = createMock(Marshaller.class);
        endpoint = new XPathAirlineEndpoint(airlineServiceMock, marshallerMock);
    }

    public void testGetFlights() throws Exception {
        org.springframework.ws.samples.airline.domain.Flight domainFlight = createDomainFlight();

        expect(airlineServiceMock.getFlights("ABC", "DEF", new LocalDate(2007, 6, 13),
                org.springframework.ws.samples.airline.domain.ServiceClass.FIRST))
                .andReturn(Collections.singletonList(domainFlight));

        replay(airlineServiceMock, marshallerMock);

        Source response = endpoint.getFlights("ABC", "DEF", "2007-06-13", "first");
        assertNotNull("No response received", response);

        verify(airlineServiceMock, marshallerMock);
    }

    private org.springframework.ws.samples.airline.domain.Flight createDomainFlight() {
        org.springframework.ws.samples.airline.domain.Flight domainFlight =
                new org.springframework.ws.samples.airline.domain.Flight();
        domainFlight.setNumber("ABC1234");
        domainFlight.setDepartureTime(new DateTime(2007, 6, 13, 12, 0, 0, 0, DateTimeZone.UTC));
        domainFlight.setFrom(new Airport("ABC", "ABC Airport", "ABC City"));
        domainFlight.setArrivalTime(new DateTime(2007, 6, 13, 14, 0, 0, 0, DateTimeZone.UTC));
        domainFlight.setTo(new Airport("DEF", "DEF Airport", "DEF City"));
        domainFlight.setServiceClass(org.springframework.ws.samples.airline.domain.ServiceClass.FIRST);
        return domainFlight;
    }

    public void testGetFrequentFlyerMileage() throws Exception {
        expect(airlineServiceMock.getFrequentFlyerMileage()).andReturn(42);

        replay(airlineServiceMock);

        Source response = endpoint.getFrequentFlyerMileage();
        assertNotNull("Invalid response", response);

        verify(airlineServiceMock);
    }


}