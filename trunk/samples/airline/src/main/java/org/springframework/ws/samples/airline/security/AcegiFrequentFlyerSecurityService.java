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

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;

import org.springframework.ws.samples.airline.dao.FrequentFlyerDao;
import org.springframework.ws.samples.airline.domain.FrequentFlyer;

/**
 * Implementation of the <code>FrequentFlyerSecurityService</code> that uses Acegi.
 *
 * @author Arjen Poutsma
 */
public class AcegiFrequentFlyerSecurityService implements FrequentFlyerSecurityService {

    private FrequentFlyerDao frequentFlyerDao;

    public void setFrequentFlyerDao(FrequentFlyerDao frequentFlyerDao) {
        this.frequentFlyerDao = frequentFlyerDao;
    }

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

    public FrequentFlyer getFrequentFlyer(String username) {
        return frequentFlyerDao.get(username);
    }
}
