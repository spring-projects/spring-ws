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

import java.io.StringWriter;
import java.util.Collections;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb2.FlightType;
import org.springframework.oxm.jaxb2.Flights;

public class Jaxb2MarshallerTest extends AbstractJaxbMarshallerTestCase {

    private static final String CONTEXT_PATH = "org.springframework.oxm.jaxb2";

    protected final Marshaller createMarshaller() throws Exception {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath(CONTEXT_PATH);
        marshaller.afterPropertiesSet();
        return marshaller;
    }

    protected Object createFlights() {
        FlightType flight = new FlightType();
        flight.setNumber(42L);
        Flights flights = new Flights();
        flights.getFlight().add(flight);
        return flights;
    }

    public void testProperties() throws Exception {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath(CONTEXT_PATH);
        marshaller.setMarshallerProperties(
                Collections.singletonMap(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE));
        marshaller.afterPropertiesSet();
    }

    public void testNoContextPathOrClassesToBeBound() throws Exception {
        try {
            Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
            marshaller.afterPropertiesSet();
            fail("Should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
        }
    }

    public void testInvalidContextPath() throws Exception {
        try {
            Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
            marshaller.setContextPath("ab");
            marshaller.afterPropertiesSet();
            fail("Should have thrown an XmlMappingException");
        }
        catch (XmlMappingException ex) {
        }
    }

    public void testMarshalInvalidClass() throws Exception {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(new Class[]{FlightType.class});
        marshaller.afterPropertiesSet();
        Result result = new StreamResult(new StringWriter());
        Flights flights = new Flights();
        try {
            marshaller.marshal(flights, result);
            fail("Should have thrown an MarshallingFailureException");
        }
        catch (XmlMappingException ex) {
            // expected
        }

    }
}
*/