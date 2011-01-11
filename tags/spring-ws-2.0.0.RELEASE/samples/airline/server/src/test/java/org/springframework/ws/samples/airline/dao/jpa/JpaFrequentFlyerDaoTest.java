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
import org.springframework.ws.samples.airline.domain.FrequentFlyer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("applicationContext-jpa.xml")
@Transactional
public class JpaFrequentFlyerDaoTest {

    @Autowired
    private JpaFrequentFlyerDao frequentFlyerDao;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Before
    public void insertTestData() {
        jdbcTemplate
                .update("INSERT INTO PASSENGER(ID, FIRST_NAME, LAST_NAME) " + "VALUES (42, 'Arjen', 'Poutsma')");
        jdbcTemplate
                .update("INSERT INTO FREQUENT_FLYER(PASSENGER_ID, USERNAME, PASSWORD, MILES) " +
                        "VALUES (42, 'arjen', 'changeme', 0)");
    }

    @Test
    public void getByUsername() throws Exception {
        FrequentFlyer flyer = frequentFlyerDao.get("arjen");
        assertNotNull("No frequent flyer returned", flyer);
        assertEquals("Invalid username", "arjen", flyer.getUsername());
        assertEquals("Invalid password", "changeme", flyer.getPassword());
        assertEquals("Invalid first name", "Arjen", flyer.getFirstName());
        assertEquals("Invalid last name", "Poutsma", flyer.getLastName());
    }

    @Test
    public void noSuchUsername() {
        FrequentFlyer flyer = frequentFlyerDao.get("invalid");
        assertNull("FrequentFlyer returned", flyer);
    }
}