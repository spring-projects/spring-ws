/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.ws.server.endpoint.mapping;

import junit.framework.TestCase;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;

public class XPathPayloadEndpointMappingTest extends TestCase {

    private XPathPayloadEndpointMapping mapping;

    protected void setUp() throws Exception {
        mapping = new XPathPayloadEndpointMapping();
    }

    public void testGetLookupKeyForMessage() throws Exception {
        mapping.setExpression("/root/text()");
        mapping.afterPropertiesSet();

        MockWebServiceMessage request = new MockWebServiceMessage("<root>value</root>");
        MessageContext context = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

        String result = mapping.getLookupKeyForMessage(context);
        assertNotNull("mapping returns null", result);
        assertEquals("mapping returns invalid result", "value", result);
    }
}