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

import java.util.Calendar;
import java.util.TimeZone;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.ws.samples.airline.domain.Airport;
import org.springframework.ws.samples.airline.domain.Customer;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.ServiceClass;
import org.springframework.ws.samples.airline.domain.Ticket;

public class HibernateTicketDaoTest extends AbstractTransactionalDataSourceSpringContextTests {

    private HibernateTicketDao dao;

    private Flight flight;

    private Calendar departureTime;

    private Calendar arrivalTime;

    private Calendar issueDate;

    private Customer customer;

    protected String[] getConfigLocations() {
        return new String[]{
                "classpath:org/springframework/ws/samples/airline/dao/hibernate/applicationContext-hibernate.xml"};
    }

    public void setDao(HibernateTicketDao dao) {
        this.dao = dao;
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
        issueDate = Calendar.getInstance();
        issueDate.set(Calendar.YEAR, 2005);
        issueDate.set(Calendar.MONTH, Calendar.DECEMBER);
        issueDate.set(Calendar.DATE, 15);
    }

    protected void onSetUpInTransaction() throws Exception {
        jdbcTemplate.update("INSERT INTO AIRPORT(CODE, NAME, CITY) VALUES('AMS', 'Schiphol Airport', 'Amsterdam')");
        Airport departureAirport = new Airport();
        departureAirport.setCode("AMS");
        departureAirport.setName("Schiphol Airport");
        departureAirport.setCity("Amsterdam");
        jdbcTemplate.update("INSERT INTO AIRPORT(CODE, NAME, CITY) VALUES('VCE', 'Marco Polo Airport', 'Venice')");
        Airport arrivalAirport = new Airport();
        arrivalAirport.setCode("VCE");
        arrivalAirport.setName("Marco Polo Airport");
        arrivalAirport.setCity("Venice");
        jdbcTemplate
                .update("INSERT INTO FLIGHT(ID, NUMBER, DEPARTURE_TIME, DEPARTURE_AIRPORT, ARRIVAL_TIME, ARRIVAL_AIRPORT, SERVICE_CLASS) " +
                        "VALUES (1, 'KL1653', '2006-01-31 10:05:00', 'AMS', '2006-01-31 12:25:00', 'VCE', 'business')");
        flight = new Flight();
        flight.setId(new Long(1));
        flight.setNumber("KL1653");
        flight.setDepartureTime(departureTime);
        flight.setDepartureAirport(departureAirport);
        flight.setArrivalTime(arrivalTime);
        flight.setArrivalAirport(arrivalAirport);
        flight.setServiceClass(ServiceClass.BUSINESS);
        jdbcTemplate.update("INSERT INTO CUSTOMER(ID, FIRST_NAME, LAST_NAME) " + "VALUES (1, 'John', 'Doe')");
        customer = new Customer();
        customer.setId(new Long(1));
        customer.setFirstName("John");
        customer.setLastName("Doe");
    }

    public void testInsert() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setCustomer(customer);
        ticket.setFlight(flight);
        ticket.setIssueDate(issueDate);
        dao.insertTicket(ticket);
        int count = jdbcTemplate.queryForInt("SELECT COUNT(0) FROM TICKET");
        assertEquals("Flight not inserted", 1, count);
    }

    public void testGet() throws Exception {
        jdbcTemplate.update(
                "INSERT INTO TICKET(ID, ISSUE_DATE, CUSTOMER_ID, FLIGHT_ID) " + "VALUES(1, '2005-12-15', 1, 1)");
        Ticket ticket = dao.getTicket(1);
        assertNotNull("Invalid ticket", ticket);
        assertEquals("Invalid ticket id", new Long(1), ticket.getId());
    }
}