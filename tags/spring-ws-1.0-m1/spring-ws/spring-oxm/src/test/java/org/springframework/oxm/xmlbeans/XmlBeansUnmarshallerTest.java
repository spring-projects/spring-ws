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
package org.springframework.oxm.xmlbeans;

import org.springframework.oxm.AbstractUnmarshallerTestCase;
import org.springframework.oxm.Unmarshaller;
import org.springframework.samples.flight.FlightType;
import org.springframework.samples.flight.FlightsDocument;
import org.springframework.samples.flight.FlightsDocument.Flights;

public class XmlBeansUnmarshallerTest extends AbstractUnmarshallerTestCase {

    protected Unmarshaller createUnmarshaller() throws Exception {
        return new XmlBeansMarshaller();
    }

    protected void testFlights(Object o) {
        FlightsDocument flightsDocument = (FlightsDocument) o;
        assertNotNull("FlightsDocument is null", flightsDocument);
        Flights flights = flightsDocument.getFlights();
        assertEquals("Invalid amount of flight elements", 1, flights.sizeOfFlightArray());
        FlightType flight = flights.getFlightArray(0);
        assertNotNull("Flight is null", flight);
        assertEquals("Number is invalid", 42L, flight.getNumber());
    }

}
