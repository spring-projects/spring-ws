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

package org.springframework.ws.samples.airline.security;

import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;

import org.springframework.dao.DataAccessException;
import org.springframework.ws.samples.airline.dao.FrequentFlyerDao;
import org.springframework.ws.samples.airline.domain.FrequentFlyer;

/**
 * Implementation of the Acegi <code>UserDetailsService</code> for <code>FrequentFlyerDetails</code>.
 *
 * @author Arjen Poutsma
 */
public class FrequentFlyerDetailsService implements UserDetailsService {

    private FrequentFlyerDao frequentFlyerDao;

    public void setFrequentFlyerDao(FrequentFlyerDao frequentFlyerDao) {
        this.frequentFlyerDao = frequentFlyerDao;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        FrequentFlyer frequentFlyer = frequentFlyerDao.get(username);
        if (frequentFlyer != null) {
            return new FrequentFlyerDetails(frequentFlyer);
        }
        else {
            throw new UsernameNotFoundException("Frequent flyer '" + username + "' not found");
        }
    }
}
