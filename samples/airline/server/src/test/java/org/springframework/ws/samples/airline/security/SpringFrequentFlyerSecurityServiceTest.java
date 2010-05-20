/*
 * Copyright 2005-2010 the original author or authors.
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
import static org.easymock.EasyMock.*;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ws.samples.airline.dao.FrequentFlyerDao;
import org.springframework.ws.samples.airline.domain.FrequentFlyer;

public class SpringFrequentFlyerSecurityServiceTest extends TestCase {

    private SpringFrequentFlyerSecurityService securityService;

    private FrequentFlyerDao flyerDaoMock;

    @Override
    protected void setUp() throws Exception {
        flyerDaoMock = createMock(FrequentFlyerDao.class);
        securityService = new SpringFrequentFlyerSecurityService(flyerDaoMock);
    }

    public void testGetCurrentlyAuthenticatedFrequentFlyer() throws Exception {
        FrequentFlyer frequentFlyer = new FrequentFlyer("john");
        FrequentFlyerDetails detail = new FrequentFlyerDetails(frequentFlyer);
        TestingAuthenticationToken token = new TestingAuthenticationToken(detail, null, new GrantedAuthority[]{});
        SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(token);
        SecurityContextHolder.setContext(context);
        replay(flyerDaoMock);
        FrequentFlyer result = securityService.getCurrentlyAuthenticatedFrequentFlyer();
        assertEquals("Invalid result", frequentFlyer, result);
        verify(flyerDaoMock);
    }

    public void testGetFrequentFlyer() throws Exception {
        FrequentFlyer frequentFlyer = new FrequentFlyer("john");
        expect(flyerDaoMock.get("john")).andReturn(frequentFlyer);
        replay(flyerDaoMock);
        FrequentFlyer result = securityService.getFrequentFlyer("john");
        assertEquals("Invalid result", frequentFlyer, result);
        verify(flyerDaoMock);
    }
}
