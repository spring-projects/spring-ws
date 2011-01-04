/*
 * Copyright 2005-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.samples.airline.dao.jpa;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.Passenger;
import org.springframework.ws.samples.airline.domain.Ticket;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("applicationContext-jpa.xml")
@Transactional
public class JpaTicketDaoTest {

    @Autowired
    private JpaTicketDao ticketDao;

    @Autowired
    private JpaFlightDao flightDao;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Before
    public void insertTestData() {
        jdbcTemplate.update("INSERT INTO AIRPORT(CODE, NAME, CITY) VALUES('RTM', 'Rotterdam Airport', 'Rotterdam')");
        jdbcTemplate.update("INSERT INTO AIRPORT(CODE, NAME, CITY) VALUES('OSL', 'Gardermoen', 'Oslo')");
        jdbcTemplate
                .update("INSERT INTO FLIGHT(ID, NUMBER, DEPARTURE_TIME, FROM_AIRPORT_CODE, ARRIVAL_TIME, TO_AIRPORT_CODE, SERVICE_CLASS, SEATS_AVAILABLE, MILES) " +
                        "VALUES (42, 'KL020','2006-01-31 10:05:00', 'RTM', '2006-01-31 12:25:00', 'OSL', 'BUSINESS', 90, 10)");
    }

    @Test
    public void save() throws Exception {
        Passenger passenger = new Passenger("Arjen", "Poutsma");
        Flight flight = flightDao.getFlight(42L);
        Ticket ticket = new Ticket();
        ticket.addPassenger(passenger);
        ticket.setFlight(flight);
        ticket.setIssueDate(new LocalDate());
        int startTicketCount = jdbcTemplate.queryForInt("SELECT COUNT(0) FROM TICKET");
        int startPassengerCount = jdbcTemplate.queryForInt("SELECT COUNT(0) FROM PASSENGER");
        ticketDao.save(ticket);
        assertNotNull("No Id generated", ticket.getId());
        int endTicketCount = jdbcTemplate.queryForInt("SELECT COUNT(0) FROM TICKET");
        int endPassengerCount = jdbcTemplate.queryForInt("SELECT COUNT(0) FROM PASSENGER");
        assertEquals("Flight not inserted", 1, endTicketCount - startTicketCount);
        assertEquals("Passenger not inserted", 1, endPassengerCount - startPassengerCount);
    }

}