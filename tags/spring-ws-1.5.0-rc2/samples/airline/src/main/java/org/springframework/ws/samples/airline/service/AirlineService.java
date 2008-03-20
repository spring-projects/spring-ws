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

import java.util.List;

import org.acegisecurity.annotation.Secured;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.FrequentFlyer;
import org.springframework.ws.samples.airline.domain.Passenger;
import org.springframework.ws.samples.airline.domain.ServiceClass;
import org.springframework.ws.samples.airline.domain.Ticket;

/**
 * Defines the business logic of the Airline application.
 *
 * @author Arjen Poutsma
 */
public interface AirlineService {

    /**
     * Returns a single <code>Flight</code> with the given id.
     *
     * @param id the flight identifier
     * @return the flight
     * @throws NoSuchFlightException if a flight with the specified flight iddoes not exist
     */
    Flight getFlight(Long id) throws NoSuchFlightException;

    /**
     * Returns a list of <code>Flight</code> objects that fall within the specified criteria.
     *
     * @param fromAirportCode the three-letter airport code to get flights from
     * @param toAirportCode   the three-letter airport code to get flights to
     * @param departureDate   the date of the flights
     * @param serviceClass    the desired service class level. May be <code>null</code>
     * @return a list of flights
     */
    List<Flight> getFlights(String fromAirportCode,
                            String toAirportCode,
                            LocalDate departureDate,
                            ServiceClass serviceClass);

    /**
     * Books a single flight for a number of passengers. Passengers can be either specified by name or by frequent flyer
     * username. If a {@link FrequentFlyer} is specified, the first and last name are looked up in the database.
     *
     * @param flightNumber  the number of the flight to book
     * @param departureTime the departure time of the flight to book
     * @param passengers    the list of passengers for the flight to book. Can be either {@link Passenger} objects with
     *                      a first and last name, or {@link FrequentFlyer} objects with a username.
     * @return the created ticket
     * @throws NoSuchFlightException        if a flight with the specified flight number and departure time does not
     *                                      exist
     * @throws NoSeatAvailableException     if not enough seats are available for the flight
     * @throws NoSuchFrequentFlyerException if a specified {@link FrequentFlyer} cannot be found
     * @see org.springframework.ws.samples.airline.domain.Passenger
     * @see org.springframework.ws.samples.airline.domain.FrequentFlyer
     */
    @Transactional(readOnly = false,
            rollbackFor = {NoSuchFlightException.class, NoSeatAvailableException.class, NoSuchFrequentFlyerException.class})
    Ticket bookFlight(String flightNumber, DateTime departureTime, List<Passenger> passengers)
            throws NoSuchFlightException, NoSeatAvailableException, NoSuchFrequentFlyerException;

    /**
     * Returns the amount of frequent flyer award miles for the currently logged in frequent flyer.
     *
     * @return the amount of frequent flyer miles
     */
    @Secured({"ROLE_FREQUENT_FLYER"})
    int getFrequentFlyerMileage();
}
