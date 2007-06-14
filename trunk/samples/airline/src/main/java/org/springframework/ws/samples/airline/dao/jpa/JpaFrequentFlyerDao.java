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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.ws.samples.airline.dao.FrequentFlyerDao;
import org.springframework.ws.samples.airline.domain.FrequentFlyer;

@Repository
public class JpaFrequentFlyerDao implements FrequentFlyerDao {

    @PersistenceContext
    private EntityManager entityManager;

    public FrequentFlyer get(String username) throws DataAccessException {
        Query query = entityManager.createQuery("FROM FrequentFlyer f WHERE f.username = :username");
        query.setParameter("username", username);
        return (FrequentFlyer) query.getSingleResult();
    }

}
