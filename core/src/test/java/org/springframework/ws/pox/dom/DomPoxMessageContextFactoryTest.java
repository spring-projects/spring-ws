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

package org.springframework.ws.pox.dom;

import junit.framework.TestCase;
import org.springframework.ws.mock.MockTransportContext;
import org.springframework.ws.mock.MockTransportRequest;

public class DomPoxMessageContextFactoryTest extends TestCase {

    private DomPoxMessageContextFactory contextFactory;

    protected void setUp() throws Exception {
        contextFactory = new DomPoxMessageContextFactory();
        contextFactory.afterPropertiesSet();
    }

    public void testCreateContext() throws Exception {
        String content = "<content/>";
        MockTransportRequest transportRequest = new MockTransportRequest(content.getBytes("UTF-8"));
        MockTransportContext transportContext = new MockTransportContext(transportRequest);
        DomPoxMessageContext context = (DomPoxMessageContext) contextFactory.createContext(transportContext);
        assertNotNull("No context returned", context);
    }
}