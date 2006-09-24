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

package org.springframework.ws.samples.airline.dao.hibernate;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.YearMonthDay;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.ws.samples.airline.domain.Airport;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.Passenger;
import org.springframework.ws.samples.airline.domain.ServiceClass;
import org.springframework.ws.samples.airline.domain.Ticket;

public class HibernateTicketDaoTest extends AbstractTransactionalDataSourceSpringContextTests {

    private HibernateTicketDao dao;

    private Flight flight;

    private DateTime departureTime;

    private DateTime arrivalTime;

    private Passenger passenger;

    protected String[] getConfigLocations() {
        return new String[]{
                "classpath:org/springframework/ws/samples/airline/dao/hibernate/applicationContext-hibernate.xml"};
    }

    public void setDao(HibernateTicketDao dao) {
        this.dao = dao;
    }

    protected void onSetUpBeforeTransaction() throws Exception {
        departureTime = new DateTime(2006, 1, 31, 10, 5, 0, 0, DateTimeZone.UTC);
        arrivalTime = new DateTime(2006, 1, 31, 12, 25, 0, 0, DateTimeZone.UTC);
    }

    protected void onSetUpInTransaction() throws Exception {
        jdbcTemplate.update("INSERT INTO AIRPORT(CODE, NAME, CITY) VALUES('RTM', 'Rotterdam Airport', 'Rotterdam')");
        Airport fromAirport = new Airport("RTM", "Rotterdam Airport", "Rotterdam");
        jdbcTemplate.update("INSERT INTO AIRPORT(CODE, NAME, CITY) VALUES('OSL', 'Gardermoen', 'Oslo')");
        Airport toAirport = new Airport("OSL", "Gardermoen", "Oslo");
        jdbcTemplate
                .update("INSERT INTO FLIGHT(ID, NUMBER, DEPARTURE_TIME, FROM_AIRPORT_CODE, ARRIVAL_TIME, TO_AIRPORT_CODE, SERVICE_CLASS, SEATS_AVAILABLE, MILES) " +
                        "VALUES (42, 'KL020','2006-01-31 10:05:00', 'RTM', '2006-01-31 12:25:00', 'OSL', 'business', 90, 10)");
        flight = new Flight();
        flight.setId(new Long(42));
        flight.setNumber("KL1653");
        flight.setDepartureTime(departureTime);
        flight.setFrom(fromAirport);
        flight.setArrivalTime(arrivalTime);
        flight.setTo(toAirport);
        flight.setServiceClass(ServiceClass.BUSINESS);
        flight.setSeatsAvailable(90);
        passenger = new Passenger();
        passenger.setFirstName("John");
        passenger.setLastName("Doe");
    }

    public void testInsert() throws Exception {
        Ticket ticket = new Ticket();
        ticket.addPassenger(passenger);
        ticket.setFlight(flight);
        ticket.setIssueDate(new YearMonthDay());
        int startTicketCount = jdbcTemplate.queryForInt("SELECT COUNT(0) FROM TICKET");
        int startPassengerCount = jdbcTemplate.queryForInt("SELECT COUNT(0) FROM PASSENGER");
        dao.save(ticket);
        int endTicketCount = jdbcTemplate.queryForInt("SELECT COUNT(0) FROM TICKET");
        int endPassengerCount = jdbcTemplate.queryForInt("SELECT COUNT(0) FROM PASSENGER");
        assertEquals("Flight not inserted", 1, endTicketCount - startTicketCount);
        assertEquals("Passenger not inserted", 1, endPassengerCount - startPassengerCount);
    }

}