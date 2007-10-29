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

import org.springframework.test.jpa.AbstractJpaTests;
import org.springframework.ws.samples.airline.dao.FrequentFlyerDao;
import org.springframework.ws.samples.airline.domain.FrequentFlyer;

public class JpaFrequentFlyerDaoTest extends AbstractJpaTests {

    private FrequentFlyerDao frequentFlyerDao;

    public void setFrequentFlyerDao(FrequentFlyerDao frequentFlyerDao) {
        this.frequentFlyerDao = frequentFlyerDao;
    }

    @Override
    protected String[] getConfigPaths() {
        return new String[]{"applicationContext-jpa.xml"};
    }

    @Override
    protected void onSetUpInTransaction() throws Exception {
        jdbcTemplate
                .update("INSERT INTO PASSENGER(ID, FIRST_NAME, LAST_NAME) " + "VALUES (42, 'Arjen', 'Poutsma')");
        jdbcTemplate
                .update("INSERT INTO FREQUENT_FLYER(PASSENGER_ID, USERNAME, PASSWORD, MILES) " +
                        "VALUES (42, 'arjen', 'changeme', 0)");
    }

    public void testGetByUsername() throws Exception {
        FrequentFlyer flyer = frequentFlyerDao.get("arjen");
        assertNotNull("No frequent flyer returned", flyer);
        assertEquals("Invalid username", "arjen", flyer.getUsername());
        assertEquals("Invalid password", "changeme", flyer.getPassword());
        assertEquals("Invalid first name", "Arjen", flyer.getFirstName());
        assertEquals("Invalid last name", "Poutsma", flyer.getLastName());
    }

    public void testNoSuchUsername() {
        FrequentFlyer flyer = frequentFlyerDao.get("invalid");
        assertNull("FrequentFlyer returned", flyer);
    }
}