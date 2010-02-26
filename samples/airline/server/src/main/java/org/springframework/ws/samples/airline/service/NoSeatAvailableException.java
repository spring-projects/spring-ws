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

package org.springframework.ws.samples.airline.service;

import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/**
 * Exception thrown when not enough seats are available for a flight.
 *
 * @author Arjen Poutsma
 */
@SoapFault(faultCode = FaultCode.SERVER)
public class NoSeatAvailableException extends Exception {

    private Flight flight;

    public NoSeatAvailableException(Flight flight) {
        super("Flight [" + flight + "] has not more seats available");
        this.flight = flight;
    }

    public Flight getFlight() {
        return flight;
    }
}
