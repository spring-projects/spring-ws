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

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.ws.samples.airline.domain.Airport;
import org.springframework.ws.samples.airline.domain.Flight;
import org.springframework.ws.samples.airline.domain.FrequentFlyer;
import org.springframework.ws.samples.airline.domain.ServiceClass;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

/**
 * A simple class that uses JPA to initialize the database.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
@Component
public class DatabaseInitializer {

    private static final Log logger = LogFactory.getLog(DatabaseInitializer.class);

    private final TransactionTemplate transactionTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    private Airport amsterdam;

    private Airport venice;

    @Autowired
    public DatabaseInitializer(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @PostConstruct
    public void initDatabase() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                logger.info("Initializing Database");
                createAirports();
                createFlights();
                createFrequentFlyers();
            }
        });
    }

    private void createAirports() {
        amsterdam = new Airport("AMS", "Schiphol Airport", "Amsterdam");
        entityManager.persist(amsterdam);
        venice = new Airport("VCE", "Marco Polo Airport", "Venice");
        entityManager.persist(venice);
    }

    private void createFlights() {
        Flight flight = new Flight();
        flight.setNumber("KL1653");
        flight.setDepartureTime(new DateTime(2006, 1, 31, 10, 5, 0, 0));
        flight.setFrom(amsterdam);
        flight.setArrivalTime(new DateTime(2006, 1, 31, 12, 25, 0, 0));
        flight.setTo(venice);
        flight.setServiceClass(ServiceClass.ECONOMY);
        flight.setSeatsAvailable(5);
        flight.setMiles(200);
        entityManager.persist(flight);
        flight = new Flight();
        flight.setNumber("KL1654");
        flight.setDepartureTime(new DateTime(2006, 2, 5, 12, 40, 0, 0));
        flight.setFrom(venice);
        flight.setArrivalTime(new DateTime(2006, 2, 5, 14, 15, 0, 0));
        flight.setTo(amsterdam);
        flight.setServiceClass(ServiceClass.ECONOMY);
        flight.setSeatsAvailable(5);
        flight.setMiles(200);
        entityManager.persist(flight);
    }

    private void createFrequentFlyers() {
        entityManager.persist(new FrequentFlyer("John", "Doe", "john", "changeme"));
    }
}
