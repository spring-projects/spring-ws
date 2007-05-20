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

import junit.framework.TestCase;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.context.SecurityContextImpl;
import org.acegisecurity.providers.TestingAuthenticationToken;
import org.easymock.MockControl;

import org.springframework.ws.samples.airline.dao.FrequentFlyerDao;
import org.springframework.ws.samples.airline.domain.FrequentFlyer;

public class AcegiFrequentFlyerSecurityServiceTest extends TestCase {

    private AcegiFrequentFlyerSecurityService securityService;

    private MockControl control;

    private FrequentFlyerDao mock;

    protected void setUp() throws Exception {
        securityService = new AcegiFrequentFlyerSecurityService();
        control = MockControl.createControl(FrequentFlyerDao.class);
        mock = (FrequentFlyerDao) control.getMock();
        securityService.setFrequentFlyerDao(mock);
    }

    public void testGetCurrentlyAuthenticatedFrequentFlyer() throws Exception {
        FrequentFlyer frequentFlyer = new FrequentFlyer("john");
        FrequentFlyerDetails detail = new FrequentFlyerDetails(frequentFlyer);
        TestingAuthenticationToken token = new TestingAuthenticationToken(detail, null, null);
        SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(token);
        SecurityContextHolder.setContext(context);
        control.replay();
        FrequentFlyer result = securityService.getCurrentlyAuthenticatedFrequentFlyer();
        assertEquals("Invalid result", frequentFlyer, result);
        control.verify();
    }

    public void testGetFrequentFlyer() throws Exception {
        FrequentFlyer frequentFlyer = new FrequentFlyer("john");
        control.expectAndReturn(mock.get("john"), frequentFlyer);
        control.replay();
        FrequentFlyer result = securityService.getFrequentFlyer("john");
        assertEquals("Invalid result", frequentFlyer, result);
        control.verify();
    }
}