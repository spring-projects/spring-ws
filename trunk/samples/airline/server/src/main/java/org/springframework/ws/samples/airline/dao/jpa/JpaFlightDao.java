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
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.ws.samples.airline.dao.FlightDao;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.ServiceClass;

@Repository
public class JpaFlightDao implements FlightDao {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Flight> findFlights(String fromAirportCode,
                                    String toAirportCode,
                                    Interval interval,
                                    ServiceClass serviceClass) throws DataAccessException {
        Query query = entityManager.createQuery("SELECT f FROM Flight f WHERE f.from.code = :fromParam " +
                "AND f.to.code = :toParam AND f.departureTime >= :start AND f.departureTime <= :end AND " +
                "f.serviceClass = :class");
        query.setParameter("fromParam", fromAirportCode);
        query.setParameter("toParam", toAirportCode);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        query.setParameter("class", serviceClass);
        return query.getResultList();
    }

    public Flight getFlight(Long id) {
        return entityManager.find(Flight.class, id);
    }

    public Flight getFlight(String flightNumber, DateTime departureTime) {
        Query query = entityManager
                .createQuery("SELECT f FROM Flight f WHERE f.number = :number AND f.departureTime = :departureTime");
        query.setParameter("number", flightNumber);
        query.setParameter("departureTime", departureTime);
        try {
            return (Flight) query.getSingleResult();
        }
        catch (NoResultException e) {
            return null;
        }
    }

    public Flight update(Flight flight) {
        return entityManager.merge(flight);
    }
}
