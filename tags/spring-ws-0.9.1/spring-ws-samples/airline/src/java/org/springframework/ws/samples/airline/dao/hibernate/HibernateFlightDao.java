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

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.ws.samples.airline.dao.FlightDao;
import org.springframework.ws.samples.airline.domain.Flight;

public class HibernateFlightDao extends HibernateDaoSupport implements FlightDao {

    public Flight getFlight(long flightId) throws DataAccessException {
        return (Flight) getHibernateTemplate().get(Flight.class, new Long(flightId));
    }

    public List getFlights(final String flightNumber, final Calendar startOfPeriod, final Calendar endOfPeriod) {
        return getHibernateTemplate().executeFind(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException {
                Criteria criteria = session.createCriteria(Flight.class);
                if (flightNumber != null) {
                    criteria.add(Expression.eq("number", flightNumber));
                }
                if (startOfPeriod != null) {
                    criteria.add(Expression.ge("departureTime", startOfPeriod));
                }
                if (endOfPeriod != null) {
                    criteria.add(Expression.le("departureTime", endOfPeriod));
                }
                return criteria.list();
            }
        });
    }

    public void insertFlight(Flight flight) throws DataAccessException {
        getHibernateTemplate().save(flight);
    }

}
