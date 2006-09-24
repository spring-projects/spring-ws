/*
 * Copyright (c) 2006, Your Corporation. All Rights Reserved.
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

/**
 * Defines the business logic for handling frequent flyers.
 *
 * @author Arjen Poutsma
 */
public interface FrequentFlyerSecurityService {

    /**
     * Returns the <code>FrequentFlyer</code> with the given username.
     *
     * @param username the username
     * @return the frequent flyer with the given username, or <code>null</code>  if not found
     */
    FrequentFlyer getFrequentFlyer(String username);

    /**
     * Returns the <code>FrequentFlyer</code> that is currently logged in.
     *
     * @return the frequent flyer that is currently logged in, or <code>null</code>  if not found
     */
    FrequentFlyer getCurrentlyAuthenticatedFrequentFlyer();

}
