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

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ws.samples.airline.dao.FrequentFlyerDao;
import org.springframework.ws.samples.airline.domain.FrequentFlyer;
import org.springframework.ws.samples.airline.service.NoSuchFrequentFlyerException;

/**
 * Implementation of the <code>FrequentFlyerSecurityService</code> that uses Spring Security.
 *
 * @author Arjen Poutsma
 */
public class SpringFrequentFlyerSecurityService implements FrequentFlyerSecurityService, UserDetailsService {

    private FrequentFlyerDao frequentFlyerDao;

    public SpringFrequentFlyerSecurityService(FrequentFlyerDao frequentFlyerDao) {
        this.frequentFlyerDao = frequentFlyerDao;
    }

    @Transactional
    public FrequentFlyer getCurrentlyAuthenticatedFrequentFlyer() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof FrequentFlyerDetails) {
                FrequentFlyerDetails details = (FrequentFlyerDetails) authentication.getPrincipal();
                return details.getFrequentFlyer();
            }
            else {
                return (FrequentFlyer) authentication.getPrincipal();
            }
        }
        else {
            return null;
        }
    }

    @Transactional
    public FrequentFlyer getFrequentFlyer(String username) throws NoSuchFrequentFlyerException {
        FrequentFlyer frequentFlyer = frequentFlyerDao.get(username);
        if (frequentFlyer != null) {
            return frequentFlyer;
        }
        else {
            throw new NoSuchFrequentFlyerException(username);
        }
    }

    @Transactional
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
