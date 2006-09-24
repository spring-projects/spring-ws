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

package org.springframework.oxm.jibx;

import org.springframework.oxm.AbstractMarshallerTestCase;
import org.springframework.oxm.Marshaller;

public class JibxMarshallerTest extends AbstractMarshallerTestCase {

    protected Marshaller createMarshaller() throws Exception {
        JibxMarshaller marshaller = new JibxMarshaller();
        marshaller.setTargetClass(Flights.class);
        marshaller.afterPropertiesSet();
        return marshaller;
    }

    protected Object createFlights() {
        Flights flights = new Flights();
        FlightType flight = new FlightType();
        flight.setNumber(42L);
        flights.addFlight(flight);
        return flights;
    }

    public void testAfterPropertiesSetNoContextPath() throws Exception {
        try {
            JibxMarshaller marshaller = new JibxMarshaller();
            marshaller.afterPropertiesSet();
            fail("Should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
        }
    }

}
