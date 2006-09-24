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

/*
Uncomment this for running JAXB2 unit tests. Make sure to that the project.properties contains

javac.source=1.5
javac.target=1.5
*/

/*
package org.springframework.oxm.jaxb;

import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.AbstractUnmarshallerTestCase;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb2.FlightType;
import org.springframework.oxm.jaxb2.Flights;

public class Jaxb2UnmarshallerTest extends AbstractUnmarshallerTestCase {

    protected Unmarshaller createUnmarshaller() throws Exception {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("org.springframework.oxm.jaxb2");
        marshaller.setSchema(new ClassPathResource("org/springframework/oxm/flight.xsd"));
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
*/