/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.soap.addressing.messageid;

import java.net.URI;

import junit.framework.TestCase;

public class RandomGuidMessageIdStrategyTest extends TestCase {

    private MessageIdStrategy strategy;

    protected final void setUp() throws Exception {
        strategy = new RandomGuidMessageIdStrategy();
    }

    public void testStrategy() {
        URI messageId1 = strategy.newMessageId(null);
        assertNotNull("Empty messageId", messageId1);
        URI messageId2 = strategy.newMessageId(null);
        assertNotNull("Empty messageId", messageId2);
        assertFalse("Equal messageIds", messageId1.equals(messageId2));
    }
}