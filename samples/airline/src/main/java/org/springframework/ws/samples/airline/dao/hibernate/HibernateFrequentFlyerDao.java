/*
 * Copyright 2006 the original author or authors.
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

import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.util.CollectionUtils;
import org.springframework.ws.samples.airline.dao.FrequentFlyerDao;
import org.springframework.ws.samples.airline.domain.FrequentFlyer;

public class HibernateFrequentFlyerDao extends HibernateDaoSupport implements FrequentFlyerDao {

    public FrequentFlyer get(String username) {
        List flyers = getHibernateTemplate().find("from FrequentFlyer f where f.username = ?", username);
        return !CollectionUtils.isEmpty(flyers) ? (FrequentFlyer) flyers.get(0) : null;
    }

    public void update(FrequentFlyer frequentFlyer) throws DataAccessException {
        getHibernateTemplate().update(frequentFlyer);
    }
}
