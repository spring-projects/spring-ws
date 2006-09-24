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
package org.springframework.oxm.castor;

import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.AbstractUnmarshallerTestCase;
import org.springframework.oxm.Unmarshaller;

public class CastorUnmarshallerTest extends AbstractUnmarshallerTestCase {

    protected void testFlights(Object o) {
        Flights flights = (Flights) o;
        assertNotNull("Flights is null", flights);
        assertEquals("Invalid amount of flight elements", 1, flights.getFlightCount());
        Flight flight = flights.getFlight()[0];
        assertNotNull("Flight is null", flight);
        assertEquals("Number is invalid", 42L, flight.getNumber());
    }

    protected Unmarshaller createUnmarshaller() throws Exception {
        CastorMarshaller marshaller = new CastorMarshaller();
        ClassPathResource mappingLocation = new ClassPathResource("mapping.xml", CastorMarshaller.class);
        marshaller.setMappingLocation(mappingLocation);
        marshaller.afterPropertiesSet();
        return marshaller;
    }

}
