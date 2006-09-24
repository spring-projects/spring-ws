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
package org.springframework.ws.samples.airline.service.impl;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.ws.samples.airline.dao.CustomerDao;
import org.springframework.ws.samples.airline.dao.FlightDao;
import org.springframework.ws.samples.airline.dao.TicketDao;
import org.springframework.ws.samples.airline.domain.Customer;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.Ticket;
import org.springframework.ws.samples.airline.service.AirlineService;

public class AirlineServiceImpl implements AirlineService {

    private final static Log logger = LogFactory.getLog(AirlineServiceImpl.class);

    private CustomerDao customerDao;

    private FlightDao flightDao;

    private TicketDao ticketDao;

    public void setCustomerDao(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    public void setFlightDao(FlightDao flightDao) {
        this.flightDao = flightDao;
    }

    public void setTicketDao(TicketDao ticketDao) {
        this.ticketDao = ticketDao;
    }

    public Ticket bookFlight(String flightNumber, long customerId) {
        List flights = flightDao.getFlights(flightNumber, null, null);
        if (CollectionUtils.isEmpty(flights)) {
            return null;
        }
        Customer customer = customerDao.getCustomer(customerId);
        Ticket ticket = new Ticket();
        ticket.setFlight((Flight) flights.get(0));
        ticket.setCustomer(customer);
        Calendar issueDate = Calendar.getInstance();
        issueDate.set(Calendar.HOUR_OF_DAY, 0);
        issueDate.set(Calendar.MINUTE, 0);
        issueDate.set(Calendar.SECOND, 0);
        issueDate.set(Calendar.MILLISECOND, 0);
        ticket.setIssueDate(issueDate);
        ticketDao.insertTicket(ticket);
        return ticket;
    }

    public List getFlightsInPeriod(String flightNumber, Calendar startOfPeriod, Calendar endOfPeriod) {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting flights in with number [" + flightNumber + "] that fall in period [" + startOfPeriod +
                    "-" + endOfPeriod + "]");
        }
        return flightDao.getFlights(flightNumber, startOfPeriod, endOfPeriod);
    }

    public List getCustomers(String name) {
        return customerDao.getCustomers(name);
    }
}
