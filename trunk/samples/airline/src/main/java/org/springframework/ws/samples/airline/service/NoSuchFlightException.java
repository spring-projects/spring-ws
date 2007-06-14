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

import org.joda.time.DateTime;

/**
 * Exception thrown when a specified flight cannot be found.
 *
 * @author Arjen Poutsma
 */
public class NoSuchFlightException extends Exception {

    private String flightNumber;

    private DateTime departureTime;

    public NoSuchFlightException(String flightNumber, DateTime departureTime) {
        super("No flight with number [" + flightNumber + "] and departure time [" + departureTime + "]");
        this.flightNumber = flightNumber;
        this.departureTime = departureTime;
    }

    public NoSuchFlightException(Long id) {
        super("No flight with id [" + id + "]");
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public DateTime getDepartureTime() {
        return departureTime;
    }
}
