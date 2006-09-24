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
package org.springframework.ws.samples.airline.dao.hibernate;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.ws.samples.airline.domain.Airport;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.ServiceClass;

public class HibernateFlightDaoTest extends AbstractTransactionalDataSourceSpringContextTests {

    private HibernateFlightDao flightDao;

    private Airport departureAirport;

    private Airport arrivalAirport;

    private Calendar departureTime;

    private Calendar arrivalTime;

    public void setFlightDao(HibernateFlightDao dao) {
        this.flightDao = dao;
    }

    protected void onSetUpBeforeTransaction() throws Exception {
        departureTime = Calendar.getInstance();
        departureTime.set(Calendar.YEAR, 2006);
        departureTime.set(Calendar.MONTH, Calendar.JANUARY);
        departureTime.set(Calendar.DATE, 31);
        departureTime.set(Calendar.HOUR_OF_DAY, 10);
        departureTime.set(Calendar.MINUTE, 5);
        departureTime.setTimeZone(TimeZone.getTimeZone("GMT+1"));
        arrivalTime = Calendar.getInstance();
        arrivalTime.set(Calendar.YEAR, 2006);
        arrivalTime.set(Calendar.MONTH, Calendar.JANUARY);
        arrivalTime.set(Calendar.DATE, 31);
        arrivalTime.set(Calendar.HOUR_OF_DAY, 12);
        arrivalTime.set(Calendar.MINUTE, 25);
        arrivalTime.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    protected void onSetUpInTransaction() throws Exception {
        jdbcTemplate.update("INSERT INTO AIRPORT(CODE, NAME, CITY) VALUES('AMS', 'Schiphol Airport', 'Amsterdam')");
        departureAirport = new Airport();
        departureAirport.setCode("AMS");
        departureAirport.setName("Schiphol Airport");
        departureAirport.setCity("Amsterdam");
        jdbcTemplate.update("INSERT INTO AIRPORT(CODE, NAME, CITY) VALUES('VCE', 'Marco Polo Airport', 'Venice')");
        arrivalAirport = new Airport();
        arrivalAirport.setCode("VCE");
        arrivalAirport.setName("Marco Polo Airport");
        arrivalAirport.setCity("Venice");
    }

    public void testInsert() {
        Flight flight = new Flight();
        flight.setNumber("1234");
        flight.setDepartureTime(departureTime);
        flight.setDepartureAirport(departureAirport);
        flight.setArrivalTime(arrivalTime);
        flight.setArrivalAirport(arrivalAirport);
        flight.setServiceClass(ServiceClass.ECONOMY);
        int startCount = jdbcTemplate.queryForInt("SELECT COUNT(0) FROM FLIGHT");
        flightDao.insertFlight(flight);
        int endCount = jdbcTemplate.queryForInt("SELECT COUNT(0) FROM FLIGHT");
        assertEquals("Flight not inserted", 1, endCount - startCount);
    }

    public void testGetById() {
        jdbcTemplate
                .update("INSERT INTO FLIGHT(ID, NUMBER, DEPARTURE_TIME, DEPARTURE_AIRPORT, ARRIVAL_TIME, ARRIVAL_AIRPORT, SERVICE_CLASS) " +
                        "VALUES (1,'KL1653','2006-01-31 10:05:00', 'AMS', '2006-01-31 12:25:00', 'VCE', 'business')");
        Flight flight = flightDao.getFlight(1);
        assertNotNull("Invalid flight", flight);
        assertEquals("Invalid flight id", new Long(1), flight.getId());
        assertEquals("Invalid flight number", "KL1653", flight.getNumber());
        assertEquals("Invalid flight service class", ServiceClass.BUSINESS, flight.getServiceClass());
    }

    public void testGetFlightsInPeriod() {
        jdbcTemplate
                .update("INSERT INTO FLIGHT(ID, NUMBER, DEPARTURE_TIME, DEPARTURE_AIRPORT, ARRIVAL_TIME, ARRIVAL_AIRPORT, SERVICE_CLASS) " +
                        "VALUES (1,'KL1653','2006-02-1 10:05:00', " + "'AMS', '2006-2-1 12:25:00', 'VCE', 'business')");
        List flights = flightDao.getFlights("KL1653", departureTime, null);
        assertNotNull("Invalid result", flights);
        assertEquals("Invalid amount of flights", 1, flights.size());

    }

    protected String[] getConfigLocations() {
        return new String[]{
                "classpath:org/springframework/ws/samples/airline/dao/hibernate/applicationContext-hibernate.xml"};
    }

}
