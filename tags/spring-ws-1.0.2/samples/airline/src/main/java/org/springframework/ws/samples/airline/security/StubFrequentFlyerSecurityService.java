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

import org.springframework.ws.samples.airline.domain.FrequentFlyer;
import org.springframework.ws.samples.airline.service.NoSuchFrequentFlyerException;

/**
 * Stub implementation of <code>FrequentFlyerSecurityService</code>. This implementation is used by default by {@link
 * org.springframework.ws.samples.airline.service.impl.AirlineServiceImpl}, to allow it to run without depending on
 * Acegi Security.
 *
 * @author Arjen Poutsma
 */
public class StubFrequentFlyerSecurityService implements FrequentFlyerSecurityService {

    private FrequentFlyer john;

    public StubFrequentFlyerSecurityService() {
        john = new FrequentFlyer("John", "Doe", "john", "changeme");
        john.setMiles(10);
    }

    public FrequentFlyer getFrequentFlyer(String username) throws NoSuchFrequentFlyerException {
        if (john.getUsername().equals(username)) {
            return john;
        }
        else {
            throw new NoSuchFrequentFlyerException(username);
        }
    }

    public FrequentFlyer getCurrentlyAuthenticatedFrequentFlyer() {
        return john;
    }
}
