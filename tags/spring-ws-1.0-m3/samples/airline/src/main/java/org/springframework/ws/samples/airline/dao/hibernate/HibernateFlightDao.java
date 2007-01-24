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

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.ws.samples.airline.dao.FlightDao;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.ServiceClass;

public class HibernateFlightDao extends HibernateDaoSupport implements FlightDao {

    public Flight getFlight(String flightNumber, DateTime departureTime) {
        List flights = getHibernateTemplate().findByNamedParam(
                "from Flight f where f.number = :number and " + "f.departureTime = :departureTime",
                new String[]{"number", "departureTime"}, new Object[]{flightNumber, departureTime});
        return !flights.isEmpty() ? (Flight) flights.get(0) : null;
    }

    public void update(Flight flight) {
        getHibernateTemplate().update(flight);
    }

    public List findFlights(String fromAirportCode, String toAirportCode, Interval interval, ServiceClass serviceClass)
            throws DataAccessException {
        return getHibernateTemplate().findByNamedParam("from Flight f where f.from.code = :from " +
                "and f.to.code = :to and " + "f.departureTime >= :start and f.departureTime <= :end and " +
                "f.serviceClass = :class", new String[]{"from", "to", "start", "end", "class"},
                new Object[]{fromAirportCode, toAirportCode, interval.getStart(), interval.getEnd(), serviceClass});
    }

}
