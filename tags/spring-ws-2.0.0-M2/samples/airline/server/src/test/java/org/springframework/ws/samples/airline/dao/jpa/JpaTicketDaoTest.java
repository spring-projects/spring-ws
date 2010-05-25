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

import org.joda.time.LocalDate;
import org.springframework.test.jpa.AbstractJpaTests;
import org.springframework.ws.samples.airline.dao.TicketDao;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.Passenger;
import org.springframework.ws.samples.airline.domain.Ticket;

public class JpaTicketDaoTest extends AbstractJpaTests {

    private TicketDao ticketDao;

    public void setTicketDao(TicketDao ticketDao) {
        this.ticketDao = ticketDao;
    }

    @Override
    protected String[] getConfigPaths() {
        return new String[]{"applicationContext-jpa.xml"};
    }

    @Override
    protected void onSetUpInTransaction() throws Exception {
        jdbcTemplate.update("INSERT INTO AIRPORT(CODE, NAME, CITY) VALUES('RTM', 'Rotterdam Airport', 'Rotterdam')");
        jdbcTemplate.update("INSERT INTO AIRPORT(CODE, NAME, CITY) VALUES('OSL', 'Gardermoen', 'Oslo')");
        jdbcTemplate
                .update("INSERT INTO FLIGHT(ID, NUMBER, DEPARTURE_TIME, FROM_AIRPORT_CODE, ARRIVAL_TIME, TO_AIRPORT_CODE, SERVICE_CLASS, SEATS_AVAILABLE, MILES) " +
                        "VALUES (42, 'KL020','2006-01-31 10:05:00', 'RTM', '2006-01-31 12:25:00', 'OSL', 'BUSINESS', 90, 10)");
    }

    public void testSave() throws Exception {
        Passenger passenger = new Passenger("Arjen", "Poutsma");
        Flight flight = sharedEntityManager.find(Flight.class, 42L);
        Ticket ticket = new Ticket();
        ticket.addPassenger(passenger);
        ticket.setFlight(flight);
        ticket.setIssueDate(new LocalDate());
        int startTicketCount = jdbcTemplate.queryForInt("SELECT COUNT(0) FROM TICKET");
        int startPassengerCount = jdbcTemplate.queryForInt("SELECT COUNT(0) FROM PASSENGER");
        ticketDao.save(ticket);
        sharedEntityManager.flush();
        assertNotNull("No Id generated", ticket.getId());
        int endTicketCount = jdbcTemplate.queryForInt("SELECT COUNT(0) FROM TICKET");
        int endPassengerCount = jdbcTemplate.queryForInt("SELECT COUNT(0) FROM PASSENGER");
        assertEquals("Flight not inserted", 1, endTicketCount - startTicketCount);
        assertEquals("Passenger not inserted", 1, endPassengerCount - startPassengerCount);
    }

}