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

package org.springframework.ws.samples.airline.dao.jpa;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.test.jpa.AbstractJpaTests;
import org.springframework.ws.samples.airline.dao.FlightDao;
import org.springframework.ws.samples.airline.domain.Airport;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.ServiceClass;

public class JpaFlightDaoTest extends AbstractJpaTests {

    private FlightDao flightDao;

    private DateTime departureTime;

    private DateTime arrivalTime;

    private Interval interval;

    private Airport fromAirport;

    private Airport toAirport;

    public void setFlightDao(FlightDao flightDao) {
        this.flightDao = flightDao;
    }

    @Override
    protected String[] getConfigPaths() {
        return new String[]{"applicationContext-jpa.xml"};
    }

    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        departureTime = new DateTime(2006, 1, 31, 10, 5, 0, 0);
        arrivalTime = new DateTime(2006, 1, 31, 12, 25, 0, 0);
        interval = departureTime.toLocalDate().toInterval();
        fromAirport = new Airport("RTM", "Rotterdam Airport", "Rotterdam");
        toAirport = new Airport("OSL", "Gardermoen", "Oslo");
    }

    @Override
    protected void onSetUpInTransaction() throws Exception {
        jdbcTemplate.update("INSERT INTO AIRPORT(CODE, NAME, CITY) VALUES('RTM', 'Rotterdam Airport', 'Rotterdam')");
        jdbcTemplate.update("INSERT INTO AIRPORT(CODE, NAME, CITY) VALUES('OSL', 'Gardermoen', 'Oslo')");
    }

    public void testGetFlightsInPeriod() throws Exception {
        jdbcTemplate
                .update("INSERT INTO FLIGHT(NUMBER, DEPARTURE_TIME, FROM_AIRPORT_CODE, ARRIVAL_TIME, TO_AIRPORT_CODE, SERVICE_CLASS, SEATS_AVAILABLE, MILES) " +
                        "VALUES ('KL020','2006-01-31 10:05:00', 'RTM', '2006-01-31 12:25:00', 'OSL', 'BUSINESS', 90, 10)");
        List<Flight> flights = flightDao.findFlights("RTM", "OSL", interval, ServiceClass.BUSINESS);
        assertNotNull("Invalid result", flights);
        assertEquals("Invalid amount of flights", 1, flights.size());
    }

    public void testGetFlightsOutOfPeriod() throws Exception {
        jdbcTemplate
                .update("INSERT INTO FLIGHT(NUMBER, DEPARTURE_TIME, FROM_AIRPORT_CODE, ARRIVAL_TIME, TO_AIRPORT_CODE, SERVICE_CLASS, SEATS_AVAILABLE, MILES) " +
                        "VALUES ('KL020','2006-01-31 10:05:00', 'RTM', '2006-01-31 12:25:00', 'OSL', 'BUSINESS', 90, 10)");
        DateTime dateTime = new DateTime(2006, 6, 1, 0, 0, 0, 0);
        List flights = flightDao.findFlights("RTM", "OSL", new Interval(dateTime, dateTime), ServiceClass.BUSINESS);
        assertNotNull("Invalid result", flights);
        assertEquals("Invalid amount of flights", 0, flights.size());
    }

    public void testGetFlightByNumberDepartureTime() throws Exception {
        jdbcTemplate
                .update("INSERT INTO FLIGHT(NUMBER, DEPARTURE_TIME, FROM_AIRPORT_CODE, ARRIVAL_TIME, TO_AIRPORT_CODE, SERVICE_CLASS, SEATS_AVAILABLE, MILES) " +
                        "VALUES ('KL020','2006-01-31 10:05:00', 'RTM', '2006-01-31 12:25:00', 'OSL', 'BUSINESS', 90, 10)");
        Flight flight = flightDao.getFlight("KL020", departureTime);
        assertNotNull("No flight returned", flight);
        assertNotNull("Invalid flight id", flight.getId());
        assertEquals("Invalid flight number", "KL020", flight.getNumber());
        assertEquals("Invalid flight departure time", departureTime, flight.getDepartureTime());
        assertEquals("Invalid flight arrival time", arrivalTime, flight.getArrivalTime());
        assertEquals("Invalid flight from airport", fromAirport, flight.getFrom());
        assertEquals("Invalid flight to airport", toAirport, flight.getTo());
        assertEquals("Invalid flight service class", ServiceClass.BUSINESS, flight.getServiceClass());
    }

    public void testUpdate() throws Exception {
        jdbcTemplate
                .update("INSERT INTO FLIGHT(NUMBER, DEPARTURE_TIME, FROM_AIRPORT_CODE, ARRIVAL_TIME, TO_AIRPORT_CODE, SERVICE_CLASS, SEATS_AVAILABLE, MILES) " +
                        "VALUES ('KL020','2006-01-31 10:05:00', 'RTM', '2006-01-31 12:25:00', 'OSL', 'BUSINESS', 90, 10)");
        Flight flight = flightDao.getFlight("KL020", departureTime);
        flight.setSeatsAvailable(0);
        flightDao.update(flight);
        sharedEntityManager.flush();
        int count = jdbcTemplate
                .queryForInt("SELECT SEATS_AVAILABLE FROM FLIGHT WHERE ID = ?", new Object[]{flight.getId()});
        assertEquals("Flight not updated", 0, count);
    }

    public void testNoSuchFlight() {
        Flight flight = flightDao.getFlight("INVALID", departureTime);
        assertNull("Flight returned", flight);
    }

}