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
package org.springframework.oxm.jaxb;

import org.springframework.oxm.AbstractUnmarshallerTest;
import org.springframework.oxm.Unmarshaller;

public class JaxbUnmarshallerTest extends AbstractUnmarshallerTest {

    protected Unmarshaller createUnmarshaller() throws Exception {
        JaxbMarshaller marshaller = new JaxbMarshaller();
        marshaller.setContextPath("org.springframework.oxm.jaxb");
        marshaller.afterPropertiesSet();
        return marshaller;
    }

    protected void testFlights(Object o) {
        Flights flights = (Flights) o;
        assertNotNull("Flights is null", flights);
        assertEquals("Invalid amount of flight elements", 1, flights.getFlight().size());
        FlightType flight = (FlightType) flights.getFlight().get(0);
        assertNotNull("Flight is null", flight);
        assertEquals("Number is invalid", 42L, flight.getNumber());
    }
}
